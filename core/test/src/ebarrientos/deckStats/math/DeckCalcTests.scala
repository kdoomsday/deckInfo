package ebarrientos.deckStats.math

import utest._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.DummyObjects._
import ebarrientos.deckStats.queries.DeckCalc
import ebarrientos.deckStats.queries.CurvePoint
import ebarrientos.deckStats.queries.CountObject
import ebarrientos.deckStats.basics.DeckEntry

object DeckCalcTests extends TestSuite {
  val d1 = Deck(Seq(DeckEntry(arthur, 2), DeckEntry(trillian, 1)), name = "d1")

  val d2 = Deck(Seq(DeckEntry(arthur, 1), DeckEntry(trillian, 1),
                    DeckEntry(ford, 1), DeckEntry(marvin, 1), DeckEntry(zaphod, 1),
                    DeckEntry(restaurant, 1), DeckEntry(heartOfGold, 1)))

  val tests = Tests {
    "Simple deck full calcs" - {
      val res = DeckCalc.fullCalc(d1)

      assert(res.avgCMC == res.avgCMCNonLands,
             res.avgCMC == 1.0,
             res.counts.contains(CountObject("Creature", 3)),
             res.manaCurve.contains(CurvePoint(1, 3)),
             res.deckName == d1.name)
    }

    "Full deck full calcs" - {
      val res = DeckCalc.fullCalc(d2)

      assert(res.avgCMC == 14.0/7.0)
      assert(res.avgCMCNonLands == 14.0/6.0)

      // Counts
      assert(res.counts.contains(CountObject("Creature", 5)),
             res.counts.contains(CountObject("Land", 1)),
             res.counts.contains(CountObject("Artifact", 2)))

      // Symbols
      assert(res.manaSymbols.contains(CountObject("W", 2)),
             res.manaSymbols.contains(CountObject("R", 3.5)))

      assert(res.manaCurve.contains(CurvePoint(1, 2)),
             res.manaCurve.contains(CurvePoint(2, 2)),
             res.manaCurve.contains(CurvePoint(5, 1)))

      assert(!res.manaCurve.contains(CurvePoint(0, 1)))
    }
  }
}

