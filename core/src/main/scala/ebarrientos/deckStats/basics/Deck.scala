package ebarrientos.deckStats.basics

/**
 * A deck of cards
 * User: Eduardo Barrientos
 * Date: 3/24/13
 * Time: 5:02 PM
 */
case class Deck(cards: Seq[DeckEntry], name: String = "") {

  /** Get the entry for a card with the given exact name
    *
    * @param name Name that is searched for
    * @return The corresponding Deckentry, if it is present
    */
  def get(name: String): Option[DeckEntry] =
    cards.filter(_.card.name == name).headOption
}

/** An entry in a deck.
  *
  * @param card  The actual card
  * @param copies The number of copies of the card. Must be greater than 0
  */
case class DeckEntry(card: Card, copies: Int) {
  require(copies > 0)
}
