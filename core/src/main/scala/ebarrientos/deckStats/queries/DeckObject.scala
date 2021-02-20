package ebarrientos.deckStats.queries

/** Response object to a deck query
  *
  * @param avgCMC         Full deck avg mana cost
  * @param avgCMCNonLands Avg mana cost not counting lands
  * @param counts         [[TypeCounts]] with card type counts for the deck
  */
case class DeckObject(avgCMC: Double, avgCMCNonLands: Double, counts: TypeCounts)

case class TypeCounts(
    lands: Int,
    creatures: Int,
    instants: Int,
    sorceries: Int,
    planeswalkers: Int,
    artifacts: Int,
    enchantments: Int
)
