package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import scalaz.zio.IO

/** Loader que nunca consigue la carta. Sirve en casos en que se quiere que
  * el helper de otro loader no consiga nada
  */
object NullCardLoader extends CardLoader {
  def card(name: String): IO[Exception, Option[Card]] = IO.sync(None)
}
