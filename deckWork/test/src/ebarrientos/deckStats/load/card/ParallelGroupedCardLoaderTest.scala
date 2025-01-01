package ebarrientos.deckStats.load.card

import utest._
import ebarrientos.deckStats.load.DummyObjects
import ebarrientos.deckStats.load.DummyObjects._
import ebarrientos.deckStats.TestHelper

object ParallelGroupedCardLoaderTest extends TestSuite{
  val testLoader = new ParallelGroupedCardLoader {
    override val maxParallelExecutions = 2
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

