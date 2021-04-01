package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.Task

/** All card loaders must implement these methods. */
trait CardLoader {
  def card(name: String): Task[Option[Card]]
}
