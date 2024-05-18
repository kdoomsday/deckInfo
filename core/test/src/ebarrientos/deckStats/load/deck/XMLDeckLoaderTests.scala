package ebarrientos.deckStats.load.deck

import utest._
import ebarrientos.deckStats.load.deck.XMLDeckLoader
import java.io.File
import ebarrientos.deckStats.DummyObjects
import ebarrientos.deckStats.TestHelper

object XMLDeckLoaderTests extends TestSuite {

  val file = new File(getClass().getClassLoader().getResource("Test.dec").getFile())

  val tests = Tests {
    val loader = new XMLDeckLoader(file, DummyObjects.dummyCardLoader)
    val loadedF = loader.load()

    val res = TestHelper.run(loadedF)

    test("numberEntries") {
      assert(res.cards.size == 2)
      res.cards.size
    }

    test("totalCards") {
      val tot = res.cards.map(_.copies).sum
      assert(tot == 8)
      tot
    }

    test("distinctCards") {
      val distinctCards = res.cards.map(_.card)
      assert(distinctCards.contains(DummyObjects.arthur))
      assert(distinctCards.contains(DummyObjects.trillian))
      assert(distinctCards.size == 2)
      distinctCards
    }

    test("numCopies") {
      res.get(DummyObjects.arthur.name).fold(assert(1==0))(e => e.copies == 4)
    }

    test("deckName") {
      assert(res.name == "The Rock X")
    }
  }
}
