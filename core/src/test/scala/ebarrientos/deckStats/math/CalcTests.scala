package ebarrientos.deckStats.math

import utest._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.DummyObjects._

object CalcTests extends TestSuite {
  val d1 = Deck(Seq(arthur, trillian))

  val d2 = Deck(Seq(arthur, trillian, ford, marvin, zaphod, restaurant))

  val tests = Tests {
    "Simple deck full calcs" - {
      val res = Calc.fullCalc(d1)

      assert(res.avgCMC == res.avgCMCNonLands)
      assert(res.avgCMC == 1.0)
    }

    "Full deck full calcs" - {
      val res = Calc.fullCalc(d2)

      assert(res.avgCMC == 9.0/6.0)
      assert(res.avgCMCNonLands == 9.0/5.0)
    }
  }
}

