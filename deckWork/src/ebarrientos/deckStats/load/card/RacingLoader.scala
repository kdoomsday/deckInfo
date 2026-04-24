package ebarrientos.deckStats.load.card


import ebarrientos.deckStats.basics.Card

import zio._
import java.time.Duration
import ebarrientos.deckStats.run.SmartRace


/**
 * Card loader that races several loaders
 *
 * @param loader  `CardLoader` used as the initial one to load
 * @param loaders Other, potentially empty `CardLoader`s to use
 * @param nextStartDelay How long to wait for a card loader before starting the next one
 */
class RacingLoader(loader: CardLoader, loaders: CardLoader*)(nextDelay: Duration = defaultDelay)
    extends CardLoader {

  override def card(name: String): Task[Option[Card]] =
    SmartRace.raceAllBackup(loader.card(name), loaders.map(_.card(name))*)(nextDelay)

}


object RacingLoader:
    val defaultDelay: Duration = 500.millis
