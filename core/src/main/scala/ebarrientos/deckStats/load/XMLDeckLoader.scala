package ebarrientos.deckStats.load

import java.io.File
import ebarrientos.deckStats.basics.{Card, Deck}
import scala.xml.Elem
import zio.Task
import java.awt.CardLayout
import zio.ZIO
import org.slf4j.LoggerFactory
import ebarrientos.deckStats.basics.DeckEntry
import scala.util.Success
import scala.util.Failure

/** Deck loader that loads the information from an XML file. The card loader provides the card
  * information.
  */
case class XMLDeckLoader(
    definition: Elem,
    loader: CardLoader
) extends DeckLoader {

  def this(file: File, loader: CardLoader) =
    this(scala.xml.XML.loadFile(file), loader)

  def this(path: String, loader: CardLoader) = this(new File(path), loader)

  val log = LoggerFactory.getLogger(getClass())

  override def load(): Task[Deck] = {
    // This will throw a NoSuchElementException if there is no main deck
    val maindeck =
      (definition \\ "zone").filter(n => (n \ "@name").text == "main").head

    val cardinfo: Seq[(String, String)] =
      for {
        card <- maindeck \ "card"
      } yield ((card \ "@name").text, (card \ "@number").text)

    log.debug("Found {} distinct cards", cardinfo.size)

    val cards: Seq[ZIO[Any, Throwable, Option[DeckEntry]]] = cardinfo.map { case (name, number) =>
      loader
        .card(name)
        .tap(c => ZIO.succeed(log.info(s"Got $c from helper loader")))
        .map(maybeCard => maybeCard.map(c => DeckEntry(c, number.toInt)))
    }

    log.debug("Collect all results into list of maybe cards")

    ZIO.collectAll(cards)
      .tap(_ => ZIO.succeed(log.debug("Map into a deck")))
      .map { cs =>
        log.debug("Begin get deck entries")
        val deckEntries = cs.flatten
        log.debug("Got entries, create deck")
        Deck(deckEntries)
      }
  }
}
