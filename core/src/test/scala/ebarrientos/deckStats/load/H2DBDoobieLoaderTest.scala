package ebarrientos.deckStats.load

import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.basics.Card
import zio.Task
import utest._
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.basics.Legendary
import zio.IO

object H2DBDoobieLoaderTest extends TestSuite {
  val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  val config = CoreConfig(dbConnectionUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                                               dbDriver        = "org.h2.Driver",
                                               dbThreads       = 1)

  val loader = new H2DBDoobieLoader(NullCardLoader, config, ec)

  val r = zio.Runtime.default

  // r.unsafeRun(loader.initTable())

  val tests = Tests {

    "Load on empty produces empty" - {
      assert( r.unsafeRun(loader.initTable().andThen(loader.card("Whatever"))).isEmpty )
    }

    "Storing and loading finds it" - {
      val arthur = Card(Seq(), "Arthur", Set(Creature), Set(Legendary), Set("Homeowner"), "Awesome", 42, 42)
      val testLoader = new CardLoader() {
        override def card(name: String) = IO { Some(arthur) }
      }

      val la = new H2DBDoobieLoader(testLoader, config, ec)
      val lb = new H2DBDoobieLoader(NullCardLoader, config, ec)

      val res = la.card("Arthur").andThen(lb.card("Arthur"))
      val ores = r.unsafeRun(res)

      assert(ores.isDefined)
      assert(ores.get == arthur)
    }

  }
}
