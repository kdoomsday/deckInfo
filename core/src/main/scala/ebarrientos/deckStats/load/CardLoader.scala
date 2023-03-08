package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.Task
import zio.ZIO

/** All card loaders must implement these methods. */
trait CardLoader {
  /**
   * Load info of a single card by name
   *
   * @param name Card name
   * @return The card, if it was found
   */
  def card(name: String): Task[Option[Card]]

  /**
   * Load several cards at once.
   *
   * The default implementation calls out to {{{card()}}}. Subclasses may choose
   * to override this function
   *
   * @param names Names to look for
   * @return {{{Seq[Card]}}}
   * All cards not found will not be present in the result
   */
  def cards(names: Seq[String]) =
    for {
      cs <- ZIO.collectAll(names.map(n => card(n)))
    } yield cs.flatten

  def cardsM(names: String*): Task[Seq[Card]] = cards(names.toSeq)
}
