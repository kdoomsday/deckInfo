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
import org.h2.jdbcx.JdbcDataSource
import ebarrientos.deckStats.config.RequestConfig
import scala.concurrent.duration.FiniteDuration
import ebarrientos.deckStats.config.Paths

/** Tests for [[H2DBQuillLoader]] */
object H2DBQuillLoaderTest extends TestSuite {

  val runner = Unsafe.unsafe(implicit unsafe => new ZioRunnerDefault()(unsafe))
  val initScriptsPath = "./dbInitScripts/"

  val ds     = {
    val jdbcds = new JdbcDataSource()
    jdbcds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    jdbcds
  }
  val loader = new H2DBQuillLoader(NullCardLoader, ds, initScriptsPath, runner)
  val r      = zio.Runtime.default

  val tests = Tests {
    test("Load on empty produces empty") {
      val res = TestHelper.run(loader.card("Whatever"))
      assert(res.isEmpty)
    }

    test("storing and loading a card finds it") {
      val la = new H2DBQuillLoader(DummyObjects.dummyCardLoader, ds, initScriptsPath, runner)
      val lb = new H2DBQuillLoader(NullCardLoader, ds, initScriptsPath, runner)

      /* We use <la> to store it. Then <lb> goes to retrieve it and it should
       * already be in the db so it should find it.
       * We use <lb> with a fallback that is guaranteed to fail to show that it
       * does not get it from that
       */

      val res  = la.card(DummyObjects.arthur.name) *> lb.card(DummyObjects.arthur.name)
      val ores = runner.run(res)

      assert(ores.isDefined)
      assert(ores.get == DummyObjects.arthur)
    }

    test("storing and loading a card with colorless mana finds it") {
      val la = new H2DBQuillLoader(DummyObjects.dummyCardLoader, ds, initScriptsPath, runner)
      val lb = new H2DBQuillLoader(NullCardLoader, ds, initScriptsPath, runner)

      /* We use <la> to store it. Then <lb> goes to retrieve it and it should
       * already be in the db so it should find it.
       * We use <lb> with a fallback that is guaranteed to fail to show that it
       * does not get it from that
       */

      val res  = la.card(DummyObjects.zaphod.name) *> lb.card(DummyObjects.zaphod.name)
      val ores = runner.run(res)

      assert(ores.isDefined)
      assert(ores.get == DummyObjects.zaphod)
    }

    test("storing and loading a card without supertypes finds it") {
      val la = new H2DBQuillLoader(DummyObjects.dummyCardLoader, ds, initScriptsPath, runner)
      val lb = new H2DBQuillLoader(NullCardLoader, ds, initScriptsPath, runner)

      val res  = la.card(DummyObjects.petunias.name) *> lb.card(DummyObjects.petunias.name)
      val ores = runner.run(res)

      assert(ores.isDefined)
      val card = ores.get
      assert(card.name == DummyObjects.petunias.name)
      assert(card.supertypes.isEmpty)
    }

    test("Loading multiple cards finds them") {
      val loader = new H2DBQuillLoader(DummyObjects.dummyCardLoader, ds, initScriptsPath, runner)
      val cards  = runner.run(
        loader.cards(
          DummyObjects.petunias.name,
          DummyObjects.heartOfGold.name,
          DummyObjects.trillian.name,
          "nonexistent"
        )
      )

      val names = cards.map(_.name)
      assert(names.contains(DummyObjects.petunias.name))
      assert(names.contains(DummyObjects.heartOfGold.name))
      assert(names.contains(DummyObjects.trillian.name))
      assert(!names.contains("nonexistent"))
    }
  }
}
