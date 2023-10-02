package ebarrientos.deckInfo.controller

import zio._
import zio.http._
import ebarrientos.deckStats.load.CardLoader
import org.slf4j.LoggerFactory
import ebarrientos.deckStats.basics.Card
import io.circe.generic.auto._
import io.circe.syntax._
import java.net.URLDecoder
import scala.xml.Elem
import scala.xml.XML
import ebarrientos.deckStats.load.XMLDeckLoader
import ebarrientos.deckStats.queries.DeckCalc

object CardController {

  private val log = LoggerFactory.getLogger(CardController.getClass())

  /** The actual app that will serve requests */
  val app: Http[CardLoader, Response, Request, Response] =
    Http
      .collectZIO[Request] {
        case Method.GET -> Root / "card" / name =>
          val unencodedName = URLDecoder.decode(name, Charsets.Utf8)
          for {
            loader <- ZIO.service[CardLoader]
            c      <- loader.card(unencodedName)
          } yield Response.json(c.getOrElse(nullCard).asJson.toString)

        case request @ Method.POST -> Root / "deck" =>
          loadDeckFromXMLRequest(request)
      }
      .catchAllZIO { ex =>
        ZIO.succeed(log.error("Error processing", ex)) *>
          ZIO.succeed(
            Response
              .text(s"Error processing. Please consult.")
              .withStatus(Status.InternalServerError)
          )
      }

  /** Card meaning no card found */
  private val nullCard: Card = Card(Seq(), "Not found", Set())

  /** Convert a string to an XML Elem */
  private def elemFromString(xmlString: String): ZIO[Any, Throwable, Elem] =
    ZIO.attempt {
      XML.loadString(xmlString)
    }

  /** Load deck stats, given a request that carries the deck in the body. The deck is expected to be
    * in an XML format that XMLDeckLoader understands
    *
    * @param request
    *   the [[Request]]
    * @return
    */
  private def loadDeckFromXMLRequest(request: Request): ZIO[CardLoader, Throwable, Response] =
    for {
      deckXMLString <- deckStringFromRequest(request)
      elem          <- elemFromString(deckXMLString)
      cardLoader    <- ZIO.service[CardLoader]
      loader        <- ZIO.succeed(new XMLDeckLoader(elem, cardLoader))
      deck          <- loader.load()
    } yield Response.json(DeckCalc.fullCalc(deck).asJson.toString)

  /** Extract the xmlDeck from the request */
  private def deckStringFromRequest(request: Request): Task[String] =
    request.body.asString.map { content =>
      val lines = content.split(Array('\n', '\r'))
      val boundary = lines.headOption.getOrElse("--")
      lines
        .dropWhile(!_.trim.startsWith("<?xml"))
        .filterNot(line => line.trim.startsWith(boundary))
        .mkString
    }
}
