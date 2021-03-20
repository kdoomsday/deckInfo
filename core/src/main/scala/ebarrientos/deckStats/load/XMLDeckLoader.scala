package ebarrientos.deckStats.load

import java.io.File
import ebarrientos.deckStats.basics.{Card, Deck}
import scala.xml.Elem
import zio.IO
import java.awt.CardLayout
import zio.ZIO
import org.slf4j.LoggerFactory

/** Deck loader that loads the information from an XML file. The card loader provides the card
  *  information.
  */
case class XMLDeckLoader(
    definition: Elem,
    loader: CardLoader,
    parallelFactor: Int = 4
) extends DeckLoader {

  def this(file: File, loader: CardLoader) =
    this(scala.xml.XML.loadFile(file), loader)

  def this(path: String, loader: CardLoader) = this(new File(path), loader)

  val log = LoggerFactory.getLogger(getClass())

  override def load(): ZIO[Any, Throwable, Deck] = {
    // This will throw a NoSuchElementException if there is no main deck
    val maindeck =
      (definition \\ "zone").filter(n => (n \ "@name").text == "main").head

    val cardinfo: Seq[(String, String)] =
      for {
        card <- maindeck \ "card"
      } yield ((card \ "@name").text, (card \ "@number").text)

    log.debug("Found {} distinct cards", cardinfo.size)

    val cards: Seq[IO[Throwable, Option[Card]]] = cardinfo.flatMap {
      case (name, number) =>
        val card = loader.card(name)
        (1 to number.toInt).map(_ => card)
    }

    log.debug("Collect all results into list of maybe cards")
    // val tmp1: IO[Throwable, Seq[Option[Card]]] = IO.collectAllParN(parallelFactor)(cards)
    val tmp1: IO[Throwable, Seq[Option[Card]]] = IO.collectAll(cards)

    log.debug("Map into a deck")
    tmp1.map(cs => Deck(cs.flatten))
  }
}
