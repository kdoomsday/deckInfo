package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

/** All card loaders must implement these methods. */
trait CardLoader {
  def card(name: String): Card
}