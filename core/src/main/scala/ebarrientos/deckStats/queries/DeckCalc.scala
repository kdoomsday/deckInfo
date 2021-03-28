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
    val counts: Seq[CountObject] =
      Calc
        .groupedCount(d, _.types)
        .map { case (t, c) => CountObject(t.toString(), c) }
        .toSeq

    val symbolCounts: Seq[CountObject] =
      Calc
        .manaSymbols(d)
        .map { case (symb, count) => CountObject(symb.toString(), count) }
        .toSeq

    DeckObject(
      avgManaCost(d),
      avgCMCNonLands = avgManaCost(d, c => !c.types.contains(Land)),
      counts,
      symbolCounts,
      Calc.manaCurve(d).map(CurvePoint.apply _)
    )
  }
}
