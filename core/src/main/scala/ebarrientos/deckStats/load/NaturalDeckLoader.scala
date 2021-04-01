package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Deck
import zio.IO
import zio.UIO
import zio.Task
import ebarrientos.deckStats.basics.DeckEntry

/** Deck loader that loads a deck that looks like:
  *
  * 4x Swords to Plowshares
  * Demonic Tutor
  * ...
  *
  * Numbers can optionally be followed by 'x'. No number means a single copy
  *
  * @param text
  * @param loader
  */
class NaturalDeckLoader(text: String, loader: CardLoader) extends DeckLoader {

  override def load(): Task[Deck] =
    Task
      .collectAllPar(
        text
          .split("\n")
          .toSeq
          .map(parseLine _)
          .map(parseCardDef _)
      )
      .map(_.flatten)
      .map(des => Deck(des))

  /** Split the line into the amount portion and the card name portion
    *
    * @param line the line to parse
    * @return Amount and card name, if present
    */
  def parseLine(line: String): Option[(String, String)] = {
    val regex = """\s*([0-9]*)\s*x?\s*(.+)""".r
    regex
      .findFirstMatchIn(line)
      .map(m => (m.group(1), m.group(2)))
  }

  def parseCardDef(odef: Option[(String, String)]): Task[Option[DeckEntry]] =
    odef
      .map { case (copiesS, name) =>
        if (name == "") Task.succeed(Option.empty)
        else {
          val amount =
            if (copiesS == "") 1
            else copiesS.toInt
          loader.card(name).map(_.map(c => DeckEntry(c, amount)))
        }
      }
      .getOrElse(Task.succeed(None))
}

object NaturalDeckLoader {
  def apply(text: String, loader: CardLoader) = new NaturalDeckLoader(text, loader)
}
