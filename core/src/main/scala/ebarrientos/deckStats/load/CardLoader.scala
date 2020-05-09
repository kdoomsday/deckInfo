package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.IO

/** All card loaders must implement these methods. */
trait CardLoader {
  def card(name: String): IO[Throwable, Option[Card]]
}
