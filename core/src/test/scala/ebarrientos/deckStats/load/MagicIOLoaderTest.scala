package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.basics.Creature
import org.slf4j.LoggerFactory

object MagicIOLoaderTest extends TestSuite {
  val log = LoggerFactory.getLogger(getClass())

  val tests = Tests {
    "Load a card successfully" - {
      val ioDC = MagicIOLoader.card("Dark Confidant")
      val t    = ioDC.map(oc =>
        oc match {
          case Some(dc) => {
            val typesStr = dc.types.mkString(", ")
            log.debug(s"Name=${dc.name}. Types=$typesStr")

            assert(dc.power == 2)
            assert(dc.toughness == 1)
            assert(dc.text.startsWith("At the beginning"))
            assert(dc.types.contains(Creature))
            assert(dc.subtypes contains "Human")
            assert(dc.subtypes contains "Wizard")
          }
          case None     => throw new Exception("No card found")
        }
      )

      zio.Runtime.default.unsafeRun(t)
    }
  }
}
