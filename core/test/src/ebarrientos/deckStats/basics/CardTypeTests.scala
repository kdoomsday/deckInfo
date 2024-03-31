package ebarrientos.deckStats.basics

import utest.*

object CardTypeTests extends TestSuite {
  val tests = Tests {
    test("load a known card type") {
      val trytype = "Creature"
      assert(CardType(trytype) == Creature)
    }

    test("load an unknown type throws") {
      intercept[Exception] {
        CardType("unknown")
      }
    }
  }
}

