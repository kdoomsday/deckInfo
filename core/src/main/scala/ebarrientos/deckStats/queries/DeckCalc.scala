package ebarrientos.deckStats.queries

import ebarrientos.deckStats.math.Calc._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Land
import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.basics.Instant
import ebarrientos.deckStats.basics.Sorcery
import ebarrientos.deckStats.basics.Planeswalker
import ebarrientos.deckStats.basics.Artifact
import ebarrientos.deckStats.basics.Enchantment

object DeckCalc {

  /** Full calculations
    *
    * @param d The deck
    * @return [[DeckObject]] with the corresponding values
    */
  def fullCalc(d: Deck): DeckObject = {
    def counts(): TypeCounts = {
      // Each card can have more than one type, so we must count individually
      var lands         = 0
      var creatures     = 0
      var instants      = 0
      var sorceries     = 0
      var planeswalkers = 0
      var artifacts     = 0
      var enchantments  = 0

      d.cards
        .foreach(c => {
          if (c.types.contains(Land))         lands += 1
          if (c.types.contains(Creature))     creatures += 1
          if (c.types.contains(Instant))      instants += 1
          if (c.types.contains(Sorcery))      sorceries += 1
          if (c.types.contains(Planeswalker)) planeswalkers += 1
          if (c.types.contains(Artifact))     artifacts += 1
          if (c.types.contains(Enchantment))  enchantments += 1
        })

      TypeCounts(
        lands,
        creatures,
        instants,
        sorceries,
        planeswalkers,
        artifacts,
        enchantments
      )
    }

    DeckObject(
      avgManaCost(d),
      avgCMCNonLands = avgManaCost(d, c => !c.types.contains(Land)),
      counts()
    )
  }
}
