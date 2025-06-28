package ebarrientos.deckStats.calc


import ebarrientos.deckStats.math.Calc._
import ebarrientos.deckStats.basics._
import ebarrientos.deckStats.math.Calc
import ebarrientos.deckStats.grouping.DeckGrouping
import ebarrientos.deckStats.queries.CardObject
import ebarrientos.deckStats.queries.DeckObject
import ebarrientos.deckStats.queries.CountObject
import ebarrientos.deckStats.queries.CurvePoint


object DeckCalc {

  @inline private def deckEntry2cardObject(de: DeckEntry): CardObject =
    CardObject(de.card.name, de.copies, de.card.multiverseId.getOrElse(-1))


  /**
   * Full calculations
   *
   * @param d The deck
   * @return [[DeckObject]] with the calculated values
   */
  def fullCalc(d: Deck, grouper: DeckGrouping): DeckObject = {
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

    val cards: Map[String, Seq[CardObject]] =
      grouper.group(d).view.mapValues(_.map(deckEntry2cardObject)).toMap

    DeckObject(
      avgCMC = avgManaCost(d),
      avgCMCNonLands = avgManaCost(d, c => !c.types.contains(CardType.Land)),
      cardCount = Calc.count(d),
      counts = counts,
      manaSymbols = symbolCounts,
      manaCurve = Calc.manaCurve(d).map(CurvePoint.apply _),
      deckName = d.name,
      cards = cards
    )
  }

}
