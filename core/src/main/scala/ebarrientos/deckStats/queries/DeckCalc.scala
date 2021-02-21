package ebarrientos.deckStats.queries

import ebarrientos.deckStats.math.Calc._
import ebarrientos.deckStats.basics._
import ebarrientos.deckStats.math.Calc

object DeckCalc {

  /** Full calculations
    *
    * @param d The deck
    * @return [[DeckObject]] with the calculated values
    */
  def fullCalc(d: Deck): DeckObject = {
    // Calculate the counts, by grouping and counting
    def counts(): TypeCounts = {
      val mapcount =
        Calc.groupedCount(d, _.types)

      TypeCounts(
        mapcount.getOrElse(Land, 0),
        mapcount.getOrElse(Creature, 0),
        mapcount.getOrElse(Instant, 0),
        mapcount.getOrElse(Sorcery, 0),
        mapcount.getOrElse(Planeswalker, 0),
        mapcount.getOrElse(Artifact, 0),
        mapcount.getOrElse(Enchantment, 0)
      )
    }

    DeckObject(
      avgManaCost(d),
      avgCMCNonLands = avgManaCost(d, c => !c.types.contains(Land)),
      counts()
    )
  }
}
