package ebarrientos.deckStats.load

import utest.TestSuite
import utest.Tests
import zio.ZIO
import ebarrientos.deckStats.DummyObjects
import ebarrientos.deckStats.TestHelper
import ebarrientos.deckStats.config.CoreConfig
import zio.Unsafe
import utest._
import ebarrientos.deckStats.run.ZioRunnerDefault


/** Tests for [[H2DBQuillLoader]] */
object H2DBQuillLoaderTest extends TestSuite {

  val config = CoreConfig(
    dbConnectionUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    // dbConnectionUrl = "jdbc:h2:file:./cards;DB_CLOSE_DELAY=-1",
    dbDriver = "org.h2.Driver",
    dbThreads = 1
  )

  val runner = Unsafe.unsafe(implicit unsafe => new ZioRunnerDefault()(unsafe))
  val loader = new H2DBQuillLoader(NullCardLoader, config, runner)
  val r = zio.Runtime.default

  /** Loader that always returns the same single card */
  val testLoader = new CardLoader() {
    override def card(name: String) = ZIO.succeed { Some(DummyObjects.arthur) }
  }

  val tests = Tests {
    test("Load on empty produces empty") {
      val res = TestHelper.run(loader.card("Whatever"))
      assert(res.isEmpty)
    }

    // test("Storing succeeds") {
    //   val la = new H2DBQuillLoader(testLoader, config, runner)
    //   val card = runner.run(la.card("Arthur Dent"))
    //   assert(card.isDefined)
    //   assert(card.get.name == "Arthur Dent")
    // }

    // test("Loading succeeds") {
    //   val la = new H2DBQuillLoader(NullCardLoader, config, runner)
    //   val card = runner.run(la.card("Arthur Dent"))
    //   assert(card.isDefined)
    //   assert(card.get.name == "Arthur Dent")
    // }

    test("Storing and loading a card finds it") {
      val la = new H2DBQuillLoader(testLoader, config, runner)
      val lb = new H2DBQuillLoader(NullCardLoader, config, runner)

      /* We use <la> to store it. Then <lb> goes to retrieve it and it should
       * already be in the db so it should find it.
       * We use <lb> with a fallback that is guaranteed to fail to show that it
       * does not get it from that
       */

      // val res  = la.card("Arthur Dent").andThen(lb.card("Arthur Dent"))
      val res  = for {
        _ <- la.card("Arthur Dent")
        card <- lb.card("Arthur Dent")
      } yield card
      val ores = runner.run(res)

      assert(ores.isDefined)
      assert(ores.get == DummyObjects.arthur)
    }
  }
}
