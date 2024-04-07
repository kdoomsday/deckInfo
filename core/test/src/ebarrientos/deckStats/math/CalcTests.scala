package ebarrientos.deckStats.math

import utest._
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.DummyObjects._
import ebarrientos.deckStats.basics.CardType.Creature
import ebarrientos.deckStats.basics.CardType.Artifact
import ebarrientos.deckStats.basics.CardType.Land
import ebarrientos.deckStats.basics.DeckEntry
import ebarrientos.deckStats.basics.CardType

object CalcTests extends TestSuite {

  val deck = Deck(
    Seq(DeckEntry(arthur, 2), DeckEntry(trillian, 1), DeckEntry(ford, 1),
        DeckEntry(marvin, 1), DeckEntry(zaphod, 1), DeckEntry(restaurant, 1),
        DeckEntry(heartOfGold, 1))
  )

  val minDeck =  Deck(Seq(DeckEntry(arthur, 2), DeckEntry(trillian, 1)))

  val tests = Tests {
    test("count") {
      test("min") {
        assert(Calc.count(minDeck) == 3)
      }

      test("groupedCount") {
        val gc: Map[CardType,Int] = Calc.groupedCount(minDeck, _.types)
        assert(gc(Creature) == 3)
        assert(gc(Land) == 0)
      }

      test("full") {
        val count = Calc.count(deck)
        assert(count == 8)
        count
      }

      test("predicate") {
        val creatures = Calc.count(deck, _.types.contains(Creature))
        val artifacts = Calc.count(deck, _.types.contains(Artifact))
        val lands     = Calc.count(deck, _.types.contains(Land))
        assert(
          creatures == 6,
          artifacts == 2,
          lands == 1
        )

        (creatures, artifacts, lands)
      }
    }

    test("average") {
      test("min") {
        val avg = Calc.avgManaCost(minDeck)
        assert(avg == 1)
        avg
      }

      test("manaCost") {
        val avg = Calc.avgManaCost(deck)
        assert(avg == 15.0 / 8.0)
        avg
      }

      test("manaCost_predicate") {
        assert(
          Calc.avgManaCost(deck, _.types.contains(Creature)) == 10.0 / 6.0,
          Calc.avgManaCost(deck, _.types.contains(Land)) == 0.0,
          Calc.avgManaCost(deck, _.types.contains(Artifact)) == 4.0
        )
      }
    }

    test("Grouping seq") {
      val ctypes = Calc.groupedCount(deck, _.types)
      assert(ctypes(Creature) == 6, ctypes(Land) == 1, ctypes(Artifact) == 2)
    }

    test("Grouping seq filter") {
      val manaCurve = Calc.groupedCount1(deck, _.cmc, !_.is(Land))
      assert(
        manaCurve(0) == 0,
        manaCurve(1) == 3,
        manaCurve(2) == 2,
        manaCurve(3) == 1,
        manaCurve(4) == 0,
        manaCurve(5) == 1
      )
    }

    test("defaultManaCurve") {
      val curve: Seq[(Int, Int)] = Calc.manaCurve(deck)
      assert(
        curve.contains(1 -> 3),
        curve.contains(2 -> 2),
        !curve.contains(0 -> 1)
      )
      curve
    }

    test("groupedManaCurve") {
      val classicCurve: Seq[(Int, Int)] = Calc.manaCurve(deck)
      val groupCurve: Seq[(Int, Int)] =
        Calc.groupedCount1(deck, groupFunc=_.cmc, cardFilter = !_.is(Land))
          .toSeq.sortBy(_._1)

      assert(classicCurve == groupCurve)
      groupCurve
    }

    test("manaSymbols") {
      val symbols: Map[String,Double] = Calc.manaSymbols(deck)
      assert(symbols("W") == 3,
             symbols("U") == 1.5,
             symbols("B") == 2,
             symbols("R") == 3.5,
             symbols("G") == 1,
             symbols("C") == 4)
      symbols
    }
  }
}
