package ebarrientos.deckStats.run


import utest._
import zio._
import ebarrientos.deckStats.TestHelper


object SmartRaceTest extends TestSuite {

  val tests = Tests {
    test("z1 returns Some first, use it") {
      val z1         = ZIO.sleep(10.millis) *> ZIO.succeed(Some(1))
      val z2         = ZIO.sleep(100.millis) *> ZIO.succeed(Some(2))
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res == Some(1))
    }

    test("z2 returns Some when z1 is slow, use it") {
      val z1         = ZIO.sleep(100.millis) *> ZIO.succeed(Some(1))
      val z2         = ZIO.sleep(10.millis) *> ZIO.succeed(Some(2))
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res == Some(2))
    }

    test("z1 returns None, result is z2's") {
      val z1         = ZIO.succeed(None)
      val z2         = ZIO.succeed(Some(2))
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res == Some(2))
    }

    test("z1 fails fast, result is z2's") {
      val z1         = ZIO.fail(new RuntimeException("boom"))
      val z2         = ZIO.succeed(Some(2))
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res == Some(2))
    }

    test("both return None, returns None") {
      val z1         = ZIO.succeed(None)
      val z2         = ZIO.succeed(None)
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res.isEmpty)
    }

    test("both fail, fails the task") {
      val z1         = ZIO.fail(new RuntimeException("boom1"))
      val z2         = ZIO.fail(new RuntimeException("boom2"))
      val startDelay = 50.millis

      val res = intercept[RuntimeException] {
        TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      }
      assert(res.getMessage.contains("boom1") || res.getMessage.contains("boom2"))
    }

    test("types for the exception align") {
      val z1         = ZIO.fail(new E1("boom1"))
      val z2         = ZIO.fail(new E2("boom2"))
      val startDelay = 50.millis

      val res = intercept[E1 | E2] {
        TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      }

      res match {
        case e1: E1 => assert(e1.getMessage == "boom1")
        case e2: E2 => assert(e2.getMessage == "boom2")
      }
    }

    test("z1 returns Some, z2 never started") {
      var z2Started  = false
      val z1         = ZIO.succeed(Some(1))
      val z2         = ZIO.succeed {
        z2Started = true
        Some(2)
      }
      val startDelay = 50.millis

      val res = TestHelper.run(SmartRace.race(z1, z2)(startDelay))
      assert(res == Some(1))
      assert(!z2Started)
    }

    test("both slow but z1 still faster") {
      val z1    = ZIO.sleep(20.millis) *> ZIO.succeed(Some(1))
      val z2    = ZIO.sleep(50.millis) *> ZIO.succeed(Some(2))
      val delay = 5.millis
      val res = TestHelper.run(SmartRace.race(z1, z2)(delay))
      assert(res == Some(1))
    }
  }


  // Exception classes for testing failures
  class E1(msg: String) extends Exception(msg)
  class E2(msg: String) extends Exception(msg)
}
