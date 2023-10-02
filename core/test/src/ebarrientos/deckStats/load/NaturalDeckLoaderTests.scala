package ebarrientos.deckStats.load

import utest.TestSuite
import utest.Tests
import utest._

import ebarrientos.deckStats.DummyObjects
import ebarrientos.deckStats.basics.DeckEntry
import ebarrientos.deckStats.TestHelper

object NaturalDeckLoaderTests extends TestSuite {
  val d1 = """|4x Arthur Dent
              | 4 Ford Prefect
              |    2xZaphod BeebleBrox
              |
              |Tricia McMillan""".stripMargin

  val tests = Tests {

    test("load") {
      val loader = new NaturalDeckLoader(d1, DummyObjects.dummyCardLoader)
      val deck = TestHelper.run(loader.load())

      assert(deck.cards contains DeckEntry(DummyObjects.arthur, 4),
             deck.cards contains DeckEntry(DummyObjects.ford, 4),
             deck.cards contains DeckEntry(DummyObjects.trillian, 1),
             deck.cards contains DeckEntry(DummyObjects.zaphod, 2))
      deck.cards.size
    }
  }
}
