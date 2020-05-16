package ebarrientos.deckStats.load

import utest._

object MagicIOLoaderTest extends TestSuite {
  val tests = Tests {
    "Load a card successfully" - {
      val ioDC = MagicIOLoader.card("Dark Confidant")
      val t = ioDC.map(oc =>  oc match {
        case Some(dc) => assert(dc.power == 2)
        case None     => throw new Exception("No card found")
      })

      zio.Runtime.default.unsafeRun(t)
    }
  }
}

