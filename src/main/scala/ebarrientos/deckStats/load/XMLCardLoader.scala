package ebarrientos.deckStats.load

import ebarrientos.deckStats.stringParsing.ManaParser
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.utils.LoadUtils
import scala.util.{ Success, Try }
import scala.xml.Elem
import scalaz.zio.IO

/** CardLoader that takes its info from an XML file. */
class XMLCardLoader(xmlFile: String) extends CardLoader with LoadUtils {
	private[this] lazy val ioCards: IO[Exception, Elem] =
    IO.fromEither(et2ee(Try(scala.xml.XML.load(xmlFile)).toEither))

  // Either[Throwable, A] => Either[Exception, A] (just wraps the throwable)
  private[this] def et2ee[A](e: Either[Throwable, A]): Either[Exception, A] =
    e match {
      case Right(a) => Right(a)
      case Left(t)  => Left(new Exception(t))
    }


	def card(name: String): IO[Exception, Option[Card]] = for(cards <- ioCards) yield {
	  // The xml find gives nodeSeq. Names are unique, so head gives only match
	  val seq = (cards \\ "card").filter(x => (x \\ "name").text == name)

	  if (seq.nonEmpty) {
		  val elem = seq.head
		  val name = (elem \ "name").text
		  val cost = (elem \ "manacost").text
		  val (supertypes, types, subtypes) = parseTypes((elem \ "type").text)
		  val text = (elem \ "text").text

		  val (power, toughness) = parsePT((elem \ "pt").text)

		  val c = Card(
	      ManaParser.parseAll(ManaParser.cost, cost).get,
		    name,
		    types, supertypes, subtypes,
		    text,
		    power,
		    toughness
      )
      Some(c)
		}
	  else None
	}
}
