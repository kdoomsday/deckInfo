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
      avgCMC         = avgManaCost(d),
      avgCMCNonLands = avgManaCost(d, c => !c.types.contains(Land)),
      cardCount      = Calc.count(d),
      counts         = counts,
      manaSymbols    = symbolCounts,
      manaCurve      = Calc.manaCurve(d).map(CurvePoint.apply _),
      deckName       = d.name,
      cards          = d.cards.map(de   => CountObject(de.card.name, de.copies))
    )
  }
}
