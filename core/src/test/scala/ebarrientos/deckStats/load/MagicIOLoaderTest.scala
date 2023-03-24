package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.TestHelper
import org.slf4j.LoggerFactory
import zio.Unsafe
import java.nio.file.Paths
import zio.ZIO
import scala.concurrent.duration.FiniteDuration
import java.time.temporal.ChronoUnit

object MagicIOLoaderTest extends TestSuite {
  val log = LoggerFactory.getLogger(getClass())

  val tests = Tests {
    "load a card from its json" - {
      val path = "core/src/test/resources/MagicIOResponse.json"
      val loader = new MagicIOLoader(timeout = FiniteDuration(100, scala.concurrent.duration.SECONDS))
      val res =
        ZIO.scoped(
          ZIO.fromAutoCloseable(ZIO.attempt(scala.io.Source.fromFile(Paths.get(path).toFile())))
            .flatMap { src =>
              val cardJsonStr = src.getLines().mkString
              loader.cardFromJsonString(name = "Dark Confidant", cardJson = cardJsonStr)
            }
            .map {
              case Some(card) =>
                assert(card.power == 2)
                assert(card.toughness == 1)
                assert(card.text.startsWith("At the beginning"))
                assert(card.types.contains(Creature))
                assert(card.subtypes contains "Human")
                assert(card.subtypes contains "Wizard")

              case None => throw new Exception("Did not parse card")
            }
        )
      TestHelper.run(res)
    }
  }
}
