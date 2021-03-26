package ebarrientos.deckStats.math

import utest._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.DummyObjects._
import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.basics.Artifact
import ebarrientos.deckStats.basics.Land

object CalcTests extends TestSuite {
  val deck = Deck(Seq(arthur, trillian, ford, marvin, zaphod, restaurant, heartOfGold))

  val tests = Tests {
    test("Simple tests") {
      test("Count") { assert(Calc.count(deck) == 7) }

      test("Count with predicate") {
        assert(Calc.count(deck, _.types.contains(Creature)) == 5,
               Calc.count(deck, _.types.contains(Artifact)) == 2,
               Calc.count(deck, _.types.contains(Land))     == 1)
      }

      test("Average mana cost") {
        val avg = Calc.avgManaCost(deck)
        assert(avg == 14.0 / 7.0)
        avg
      }

      test("Average cost with predicate") {
        assert(Calc.avgManaCost(deck, _.types.contains(Creature)) == 9.0/5.0,
               Calc.avgManaCost(deck, _.types.contains(Land))     == 0.0,
               Calc.avgManaCost(deck, _.types.contains(Artifact)) == 4.0)
      }

      test("Grouping seq") {
        val ctypes = Calc.groupedCount(deck, _.types)
        assert(ctypes(Creature) == 5,
               ctypes(Land)     == 1,
               ctypes(Artifact) == 2)
      }

      test("Grouping seq filter") {
        val manaCurve = Calc.groupedCount1(deck, _.cmc, !_.is(Land))
        assert(manaCurve(0) == 0,
               manaCurve(1) == 2,
               manaCurve(2) == 2,
               manaCurve(3) == 1,
               manaCurve(4) == 0,
               manaCurve(5) == 1)
      }

      test("Default Mana Curve") {
        val curve: Seq[(Int, Int)] = Calc.manaCurve(deck)
        assert(curve contains (1, 2),
               curve contains (2, 2),
               !curve.contains(0 -> 1))
      }
    }
  }
}

