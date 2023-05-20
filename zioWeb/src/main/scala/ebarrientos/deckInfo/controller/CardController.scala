package ebarrientos.deckInfo.controller

import zio._
import zio.http._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.load.MagicIOLoader
import ebarrientos.deckStats.load.XMLCardLoader
import ebarrientos.deckStats.load.SequenceLoader
import ebarrientos.deckStats.load.H2DBQuillLoader
import org.slf4j.LoggerFactory
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource
import ebarrientos.deckStats.run.ZioRunner
import ebarrientos.deckStats.run.ZioRunnerDefault
import ebarrientos.deckStats.basics.Card
import io.circe.generic.auto._
import io.circe.syntax._
import java.net.URLDecoder
import ebarrientos.deckStats.load.DeckLoader
import scala.xml.Elem
import scala.xml.XML
import ebarrientos.deckStats.load.XMLDeckLoader
import ebarrientos.deckStats.queries.DeckCalc
import java.io.File
import zio.stream.ZSink

object CardController {

  private val log = LoggerFactory.getLogger(CardController.getClass())

  /** The actual app that will serve requests */
  val app: Http[CardLoader, Response, Request, Response] =
    Http
      .collectZIO[Request] {
        case Method.GET -> !! / "card" / name =>
          val unencodedName = URLDecoder.decode(name, Charsets.Utf8)
          for {
            loader <- ZIO.service[CardLoader]
            c      <- loader.card(unencodedName)
          } yield Response.json(c.getOrElse(nullCard).asJson.toString)

        case request @ Method.POST -> !! / "deck" =>
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
      _             <- ZIO.succeed(log.info(s">>> $deckXMLString"))
      elem          <- elemFromString(deckXMLString)
      cardLoader    <- ZIO.service[CardLoader]
      loader        <- ZIO.succeed(new XMLDeckLoader(elem, cardLoader))
      deck          <- loader.load()
    } yield Response.json(DeckCalc.fullCalc(deck).asJson.toString)

  /** Extract the xmlDeck from the request */
  private def deckStringFromRequest(request: Request): Task[String] =
    request.body.asString.map { content =>
      content
        .split(Array('\n', '\r'))
        .filter(_.trim.startsWith("<")) // This depends on xml elements being each on a line. Do better
        .mkString
    }
}
