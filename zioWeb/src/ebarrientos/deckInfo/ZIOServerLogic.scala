package ebarrientos.deckInfo

import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.queries.DeckObject
import zio.ZIO
import ebarrientos.deckStats.queries.DeckCalc
import ebarrientos.deckStats.load.XMLDeckLoader
import scala.xml.Elem
import scala.xml.XML

class ZIOServerLogic(cardLoader: CardLoader) {
  import ZIOServerLogic.urlDecode

  def card(name: String): ZIO[Any, Throwable, Option[Card]] =
    cardLoader.card(urlDecode(name))

  def deck(stringXmlContent: String): ZIO[Any, Nothing, Either[Unit, DeckObject]] = {
    (for {
      elem          <- elemFromString(stringXmlContent)
      loader        <- ZIO.succeed(new XMLDeckLoader(elem, cardLoader))
      deck          <- loader.load()
      calc <- ZIO.succeed(DeckCalc.fullCalc(deck))
    } yield calc)
      .either
      .map(_.left.map(_ => ()))
  }


  /** Convert a string to an XML Elem */
  private def elemFromString(xmlString: String): ZIO[Any, Throwable, Elem] =
    ZIO.attempt {
      XML.loadString(xmlString)
    }
}

object ZIOServerLogic {
  @inline def urlDecode(raw: String): String =
    java.net.URLDecoder.decode(raw, "utf-8")
}

