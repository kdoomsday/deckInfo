package ebarrientos.deckStats.load.card

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
  def cards(names: Seq[String]): ZIO[Any,Throwable,Seq[Card]] =
    for {
      cs <- ZIO.collectAllSuccesses(names.map(card))
    } yield cs.flatten

  /** Load cards by name. Utility present for usability purposes */
  def cards(name: String, names: String*): ZIO[Any,Throwable,Seq[Card]] =
    cards(name +: names)
}
