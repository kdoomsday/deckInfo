package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Deck
import zio.IO

/** All deck loaders must implement these methods. */
trait DeckLoader {
  def load(): IO[Throwable, Deck]
}
