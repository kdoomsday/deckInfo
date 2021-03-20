package ebarrientos.deckStats

import utest._
import ebarrientos.deckStats.load.XMLDeckLoader
import java.nio.file.Paths
import java.io.File

object XMLDeckLoaderTests extends TestSuite {

  val file = new File(getClass().getClassLoader().getResource("Test.dec").getFile())

  val tests = Tests {
    test("Load test deck") {
      val loader = new XMLDeckLoader(file, DummyObjects.dummyCardLoader)
      val loadedF = loader.load()

      val res = zio.Runtime.default.unsafeRun(loadedF)
      assert(res.cards.size == 8)
      assert(res.cards.contains(DummyObjects.arthur))
      assert(res.cards.contains(DummyObjects.trillian))

      assert(res.cards.groupBy(identity)(DummyObjects.arthur).size == 4)
    }
  }
}
