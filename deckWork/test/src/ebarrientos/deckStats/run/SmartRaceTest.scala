package ebarrientos.deckStats.run


import utest._
import zio._
import ebarrientos.deckStats.TestHelper


object SmartRaceTest extends TestSuite {

  val tests = Tests {
    test("z1 returns Some first, use it") {
      val z1    = ZIO.sleep(10.millis) *> ZIO.succeed(Some(1))
      val z2    = ZIO.sleep(100.millis) *> ZIO.succeed(Some(2))
      val delay = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(1))
    }

    test("z2 returns Some when z1 is slow, use it") {
      val z1    = ZIO.sleep(100.millis) *> ZIO.some(1)
      val z2    = ZIO.sleep(10.millis) *> ZIO.some(2)
      val delay = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(2))
    }

    test("z1 returns None, result is z2's") {
      val z1    = ZIO.none
      val z2    = ZIO.some(2)
      val delay = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(2))
    }

    test("z1 fails fast, result is z2's") {
      val z1    = ZIO.fail(new RuntimeException("boom"))
      val z2    = ZIO.some(2)
      val delay = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(2))
    }

    test("both return None, returns None") {
      val z1    = ZIO.none
      val z2    = ZIO.none
      val delay = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res.isEmpty)
    }

    test("z1 fails, z2 is None, result is None") {
      val z1    = ZIO.fail(new Exception("boom"))
      val z2    = ZIO.none
      val delay = 20.millis
      val res   = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == None)
    }

    test("z1 is None, z2 fails, result is None") {
      val z1    = ZIO.none
      val z2    = ZIO.fail(new Exception("boom"))
      val delay = 10.millis
      val res   = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == None)
    }

    test("both fail, fails the task") {
      val z1    = ZIO.fail(new Exception("boom1"))
      val z2    = ZIO.fail(new Exception("boom2"))
      val delay = 50.millis

      val res = intercept[Exception] {
        TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      }
      assert(res.getMessage.contains("boom1") || res.getMessage.contains("boom2"))
    }

    test("types for the exception align") {
      val z1    = ZIO.fail(new E1("boom1"))
      val z2    = ZIO.fail(new E2("boom2"))
      val delay = 50.millis

      val res = intercept[E1 | E2] {
        TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      }

      res match {
        case e1: E1 => assert(e1.getMessage == "boom1")
        case e2: E2 => assert(e2.getMessage == "boom2")
      }
    }

    test("z1 returns Some, z2 never started") {
      var z2Started = false
      val z1        = ZIO.some(1)
      val z2        = ZIO.succeed {
        z2Started = true
        Some(2)
      }
      val delay     = 50.millis

      val res = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(1))
      assert(!z2Started)
    }

    test("both slow but z1 still faster") {
      val z1    = ZIO.sleep(20.millis) *> ZIO.some(1)
      val z2    = ZIO.sleep(50.millis) *> ZIO.some(2)
      val delay = 5.millis
      val res   = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(1))
    }

    test("if z1 finishes z2 is started without waiting for the delay") {
      val start         = java.lang.System.currentTimeMillis()
      val z1            = ZIO.none
      val z2            = ZIO.some(2)
      val delay         = 500.millis
      val res           = TestHelper.run(SmartRace.raceSome(z1, z2)(delay))
      assert(res == Some(2))
      val totalDuration = (java.lang.System.currentTimeMillis() - start).millis
      assert(totalDuration < delay)
    }

    test("z2 starts and returns none before z1 finishes with some, return z1's value") {
      val z1    = ZIO.sleep(50.millis) *> ZIO.some(1)
      val z2    = ZIO.none
      val delay = 10.millis
      assert(TestHelper.run(SmartRace.raceSome(z1, z2)(delay)) == Some(1))
    }

    test("z2 is called only once, even if z1 finds nothing") {
      val run         = Ref.make(0).flatMap { runs =>
          val z1    = ZIO.none
          val z2    = runs.getAndUpdate(_ + 1) *> ZIO.sleep(50.millis) *> ZIO.some(2)
          val delay = 25.millis
          (SmartRace.raceSome(z1, z2)(delay)).zip(runs.get)
      }
      val (res, runs) = TestHelper.run(run)
      assert(res == Some(2))
      assert(runs == 1)
    }

    test("minitest - failure") {
      val z1  = ZIO.fail(new Exception("boom"))
      val z2  = ZIO.sleep(10.millis) *> ZIO.succeed(2)
      val res = TestHelper.run(z1.race(z2))
      assert(res == 2)
    }

    test("minitest - failure on slow path") {
      val z1  = ZIO.succeed(1)
      val z2  = ZIO.sleep(40.millis) *> ZIO.fail(new Exception("boom 2"))
      val res = TestHelper.run(z1.race(z2))
      assert(res == 1)
    }

    test("minitest - fail flatmap") {
      val z = ZIO
        .attempt[Int](throw new Exception("boom"))
        .flatMap(i => ZIO.succeed(i + 1))
      intercept[Exception] {
        TestHelper.run(z)
      }
    }
  }


  // Exception classes for testing failures
  class E1(msg: String) extends Exception(msg)
  class E2(msg: String) extends Exception(msg)
}
