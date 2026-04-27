package ebarrientos.deckStats.load.card


import ebarrientos.deckStats.basics.Card
import zio.Task
import zio.ZIO


/** Can fetch card information by name from some source */
trait CardLoader {

  /**
   * Load info of a single card by name
   *
   * @param name Card name
   * @return The card, if it was found
   */
  def card(name: String): Task[Option[Card]]


  /**
   * Load several cards at once. All cards not found will not be present in
   * the result and should not, as per contract.
   *
   * The default implementation calls out to `card()` for each name.
   * Subclasses may choose to override this function, in particular if they can
   * do better than simply calling each card at a time
   *
   * @param names Names of cards to look for
   * @return `Seq[Card]` with the fetched cards
   */
  def cards(names: Seq[String]): ZIO[Any, Throwable, Seq[Card]] =
    for {
      cs <- ZIO.collectAllSuccessesPar(names.map(card))
    } yield cs.flatten


  /** Load cards by name. Utility present for usability purposes */
  def cards(name: String, names: String*): ZIO[Any, Throwable, Seq[Card]] =
    cards(name +: names)

}
