package models

/** Response object to a deck query
  *
  * @param avgCMC         Full deck avg mana cost
  * @param avgCMCNonLands Avg mana cost not counting lands
  */
case class DeckObject(avgCMC: Double,
                      avgCMCNonLands: Double)

