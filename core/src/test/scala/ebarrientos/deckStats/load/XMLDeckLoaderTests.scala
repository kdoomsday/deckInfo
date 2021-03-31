package ebarrientos.deckStats

import utest._
import ebarrientos.deckStats.load.XMLDeckLoader
import java.nio.file.Paths
import java.io.File
import ebarrientos.deckStats.basics.DeckEntry

object XMLDeckLoaderTests extends TestSuite {

  val file = new File(getClass().getClassLoader().getResource("Test.dec").getFile())

  val tests = Tests {
    val loader = new XMLDeckLoader(file, DummyObjects.dummyCardLoader)
    val loadedF = loader.load()

    val res = zio.Runtime.default.unsafeRun(loadedF)

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
  }
}
