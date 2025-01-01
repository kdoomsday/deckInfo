package ebarrientos.deckStats.grouping

import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.DeckEntry

trait DeckGrouping {
  type GroupName = String

  /**
   * Group information of a deck
   *
   * @param deck The [[Deck]] to operate on
   * @return Map from GroupName to the deck entries in that group
   */
  def group(deck: Deck): Map[GroupName, Seq[DeckEntry]]
}
