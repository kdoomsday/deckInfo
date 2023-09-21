package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Deck
import zio.IO

/** All deck loaders must implement these methods.
  * The [[load()]] method takes nothing because different loaders can have
  * different types of sources
  */
trait DeckLoader {

  /** Load the deck */
  def load(): IO[Throwable, Deck]
}
