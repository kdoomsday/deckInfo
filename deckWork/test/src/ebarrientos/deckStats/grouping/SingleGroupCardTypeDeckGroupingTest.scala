package ebarrientos.deckStats.grouping

import utest._
import ebarrientos.deckStats.load.DummyObjects.*
import ebarrientos.deckStats.basics.CardType.*
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.DeckEntry

object SingleGroupCardTypeDeckGroupingTest extends TestSuite {

  val tests = Tests {
    test("basic group for each card") {
      val testEntries: Seq[DeckEntry] =
        Seq(arthur, marvin, heartOfGold, restaurant)
          .zipWithIndex
          .map((c, i) => DeckEntry(c, i + 1))

      val testDeck = Deck(testEntries)
      val res      = SingleGroupCardTypeDeckGrouping.group(testDeck)

      // assert(res.keySet.size == 3)
      assert(res(Creature.toString).size == 2)
      assert(res(Artifact.toString).size == 1)
      assert(res(Land.toString).size == 1)
      assert(res(Planeswalker.toString).size == 0)
      assert(res(Enchantment.toString).size == 0)
      assert(res(Battle.toString).size == 0)
    }
  }
}
