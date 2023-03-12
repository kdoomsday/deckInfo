package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.DummyObjects
import ebarrientos.deckStats.DummyObjects._
import ebarrientos.deckStats.TestHelper

object ParallelCardLoaderTest extends TestSuite{
  val testLoader = new ParallelCardLoader {
    override def card(name: String) = DummyObjects.dummyCardLoader.card(name)
  }

  val tests = Tests {
    test("loads multiple cards correctly") {
      val names = Seq(arthur.name, heartOfGold.name, trillian.name)
      val cardRes = TestHelper.run(testLoader.cards(names)).map(_.name)
      assert(cardRes.size == 3)
      names.foreach(name => assert(cardRes.contains(name)))
      cardRes
    }
  }
}

