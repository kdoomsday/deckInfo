package ebarrientos.deckStats.math

import utest._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.DummyObjects._
import ebarrientos.deckStats.queries.DeckCalc
import ebarrientos.deckStats.queries.CurvePoint

object DeckCalcTests extends TestSuite {
  val d1 = Deck(Seq(arthur, trillian))

  val d2 = Deck(Seq(arthur, trillian, ford, marvin, zaphod, restaurant, heartOfGold))

  val tests = Tests {
    "Simple deck full calcs" - {
      val res = DeckCalc.fullCalc(d1)

      assert(res.avgCMC == res.avgCMCNonLands,
             res.avgCMC == 1.0,
             res.counts.creatures == 2,
             res.manaCurve.contains(CurvePoint(1, 2)))
    }

    "Full deck full calcs" - {
      val res = DeckCalc.fullCalc(d2)

      assert(res.avgCMC == 14.0/7.0)
      assert(res.avgCMCNonLands == 14.0/6.0)

      // Counts
      assert(res.counts.creatures == 5)
      assert(res.counts.lands == 1)
      assert(res.counts.artifacts == 2)
      assert(res.counts.enchantments == 0)

      assert(res.manaCurve.contains(CurvePoint(1, 2)),
             res.manaCurve.contains(CurvePoint(2, 2)),
             res.manaCurve.contains(CurvePoint(5, 1)))

      assert(!res.manaCurve.contains(CurvePoint(0, 1)))
    }
  }
}

