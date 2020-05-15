package ebarrientos.deckStats.load

import java.io.File
import ebarrientos.deckStats.basics.{ Deck, Card }
import scala.xml.Elem
import zio.IO
import java.awt.CardLayout

/** Deck loader that loads the information from an XML file. The card loader provides the card
  *  information.
  */
case class XMLDeckLoader(definition: Elem, loader: CardLoader) extends DeckLoader {
  def this(file: File, loader: CardLoader) = this(scala.xml.XML.loadFile(file), loader)
  def this(path: String, loader: CardLoader) = this(new File(path), loader)

  def load = {
    // This will throw a NoSuchElementException if there is no main deck
    val maindeck = (definition \\ "zone").filter(n => (n \ "@name").text == "main").head


    val cardinfo: Seq[(String, String)] =
      for {
        card <- maindeck \ "card"
      } yield ((card \ "@name").text, (card \ "@number").text)

    val cards: Seq[IO[Throwable, Option[Card]]] = cardinfo.flatMap {
      case (name, number) =>
        val card = loader.card(name)
        (1 to number.toInt).map(_ => card)
    }

    IO.collectAll(cards).map(cs => Deck(cs.flatten))
  }
}
