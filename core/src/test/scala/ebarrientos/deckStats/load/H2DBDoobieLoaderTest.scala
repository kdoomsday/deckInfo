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
import ebarrientos.deckStats.DummyObjects

/** Tests for [[H2DBDoobieLoader]] */
object H2DBDoobieLoaderTest extends TestSuite {
  val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  val config = CoreConfig(dbConnectionUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                                               dbDriver        = "org.h2.Driver",
                                               dbThreads       = 1)

  val loader = new H2DBDoobieLoader(NullCardLoader, config, ec)

  /** Loader that always returns the same single card */
  val testLoader = new CardLoader() {
    override def card(name: String) = IO { Some(DummyObjects.arthur) }
  }

  val r = zio.Runtime.default

  val tests = Tests {
    test("Load on empty produces empty") {
      assert( r.unsafeRun(loader.initTable().andThen(loader.card("Whatever"))).isEmpty )
    }

    test("Storing and loading a card finds it") {
      val la = new H2DBDoobieLoader(testLoader, config, ec)
      val lb = new H2DBDoobieLoader(NullCardLoader, config, ec)

      /* We use la to store it. Then lb goes to retrieve it and it should
       * already be in the db so it should find it.
       * We use lb with a fallback that is guaranteed to fail to show that it
       * does not get it from that
       */

      val res = la.card("Arthur Dent").andThen(lb.card("Arthur Dent"))
      val ores = r.unsafeRun(res)

      assert(ores.isDefined)
      assert(ores.get == DummyObjects.arthur)
    }

  }
}
