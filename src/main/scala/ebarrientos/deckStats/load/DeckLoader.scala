package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Deck

/** All deck loaders must implement these methods. */
trait DeckLoader {
  def load(): Deck
}