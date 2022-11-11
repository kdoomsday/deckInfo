package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio._

/** Loader que nunca consigue la carta. Sirve en casos en que se quiere que
  * el helper de otro loader no consiga nada
  */
object NullCardLoader extends CardLoader {
  def card(name: String): Task[Option[Card]] = ZIO.succeed(None)
}
