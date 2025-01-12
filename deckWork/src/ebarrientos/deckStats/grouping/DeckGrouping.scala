package ebarrientos.deckStats.grouping

import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.DeckEntry

trait DeckGrouping {

  /**
   * Group information of a deck
   *
   * @param deck The [[Deck]] to operate on
   * @return Map from group names to the deck entries in that group
   */
  def group(deck: Deck): Map[String, Seq[DeckEntry]]
}
