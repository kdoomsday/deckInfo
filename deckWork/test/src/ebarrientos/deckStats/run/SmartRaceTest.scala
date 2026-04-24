package ebarrientos.deckStats.run


import utest._
import zio._
import ebarrientos.deckStats.TestHelper
import scala.annotation.nowarn


object SmartRaceTest extends TestSuite {

  val tests = Tests {
    test("raceBackup") {
      test("z1 returns Some first, use it") {
        val z1    = ZIO.sleep(10.millis) *> ZIO.succeed(Some(1))
        val z2    = ZIO.sleep(100.millis) *> ZIO.succeed(Some(2))
        val delay = 50.millis

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(1))
      }

      test("z2 returns Some when z1 is slow, use it") {
        val z1    = ZIO.sleep(100.millis) *> ZIO.some(1)
        val z2    = ZIO.sleep(10.millis) *> ZIO.some(2)
        val delay = 50.millis

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(2))
      }

      test("z1 returns None, result is z2's") {
        val z1    = ZIO.none
        val z2    = ZIO.some(2)
        val delay = 50.millis

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(2))
      }

      test("z1 fails fast, result is z2's") {
        val z1    = ZIO.fail(new RuntimeException("boom"))
        val z2    = ZIO.some(2)
        val delay = 50.millis

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(2))
      }

      test("both return None, returns None") {
        val z1    = ZIO.none
        val z2    = ZIO.none
        val delay = 50.millis

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res.isEmpty)
      }

      test("z1 fails, z2 is None, result is None") {
        val z1    = ZIO.fail(new Exception("boom"))
        val z2    = ZIO.none
        val delay = 20.millis
        val res   = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == None)
      }

      test("z1 is None, z2 fails, result is None") {
        val z1    = ZIO.none
        val z2    = ZIO.fail(new Exception("boom"))
        val delay = 10.millis
        val res   = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == None)
      }

      test("both fail, fails the task") {
        val z1    = ZIO.fail(new Exception("boom1"))
        val z2    = ZIO.fail(new Exception("boom2"))
        val delay = 50.millis

        val res = intercept[Exception] {
          TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        }
        assert(res.getMessage.contains("boom1") || res.getMessage.contains("boom2"))
      }

      test("types for the exception align") {
        val z1    = ZIO.fail(new E1("boom1"))
        val z2    = ZIO.fail(new E2("boom2"))
        val delay = 50.millis

        val res = intercept[E1 | E2] {
          TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
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

        val res = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(1))
        assert(!z2Started)
      }

      test("both slow but z1 still faster") {
        val z1    = ZIO.sleep(20.millis) *> ZIO.some(1)
        val z2    = ZIO.sleep(50.millis) *> ZIO.some(2)
        val delay = 5.millis
        val res   = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(1))
      }

      test("if z1 finishes z2 is started without waiting for the delay") {
        val start         = java.lang.System.currentTimeMillis()
        val z1            = ZIO.none
        val z2            = ZIO.some(2)
        val delay         = 500.millis
        val res           = TestHelper.run(SmartRace.raceBackup(z1, z2)(delay))
        assert(res == Some(2))
        val totalDuration = (java.lang.System.currentTimeMillis() - start).millis
        assert(totalDuration < delay)
      }

      test("z2 starts and returns none before z1 finishes with some, return z1's value") {
        val z1    = ZIO.sleep(50.millis) *> ZIO.some(1)
        val z2    = ZIO.none
        val delay = 10.millis
        assert(TestHelper.run(SmartRace.raceBackup(z1, z2)(delay)) == Some(1))
      }

      test("z2 is called only once, even if z1 finds nothing") {
        val run         = Ref.make(0).flatMap { runs =>
            val z1    = ZIO.none
            val z2    = runs.getAndUpdate(_ + 1) *> ZIO.sleep(50.millis) *> ZIO.some(2)
            val delay = 25.millis
            (SmartRace.raceBackup(z1, z2)(delay)).zip(runs.get)
        }
        val (res, runs) = TestHelper.run(run)
        assert(res == Some(2))
        assert(runs == 1)
      }
    }
    test("raceAllBackup") {
      test("2 loader case is the same as raceBackup") {
        val delay = 10.millis

        case class TestCase(d1: Duration, d2: Duration, startDelay: Duration)
        val cases = List(
          TestCase(10.millis, 100.millis, 10.millis), // 1 finishes first
          TestCase(100.millis, 10.millis, 10.millis), // 2 finishes first
          TestCase(10.millis,   1.millis, 50.millis)  // 1 finishes without starting 2
        )

        cases.foreach { case TestCase(d1, d2, startDelay) =>
          val z1 = ZIO.sleep(d1) *> ZIO.some(1)
          val z2 = ZIO.sleep(d2) *> ZIO.some(2)

          val zall = SmartRace.raceAllBackup(z1, z2)(startDelay)
          val z = SmartRace.raceBackup(z1, z2)(startDelay)
          val (rAll, r) = TestHelper.run(zall.zip(z))
          assert(rAll == r)
        }
      }

      test("simple 3 loader case where 3 succeeds") {
        val z1  = ZIO.fail(new Exception("boom1"))
        val z2  = ZIO.fail(new Exception("boom2"))
        val z3  = ZIO.some(3)
        val res = TestHelper.run {
          SmartRace.raceAllBackup(z1, z2, z3)(10.millis)
        }
        assert(res == Some(3))
      }


      test("first loader wins, others not called") {
        var started2 = false
        var started3 = false
        val z1  = ZIO.some(1)
        val z2  = ZIO.succeed { started2 = true; Some(2) }
        val z3  = ZIO.succeed { started3 = true; Some(3) }
        val res = TestHelper.run {
          SmartRace.raceAllBackup(z1, z2, z3)(10.millis)
        }
        assert(res == Some(1))
        assert(!started2)
        assert(!started3)
      }

      test("order doesn't matter if a single one succeeds") {
        val z1: Task[Option[Int]] = ZIO.some(1)
        val z2: Task[Option[Int]] = ZIO.fail(new Exception("boom2"))
        val z3: Task[Option[Int]] = ZIO.fail(new Exception("boom3"))

        @nowarn("msg=match may not be exhaustive")
        val all = (z1, z2, z3).toList.permutations.map { case List(a, b, c) => (a, b, c) }

        assert(
          all.map { case (a, b, c) => TestHelper.run(SmartRace.raceAllBackup(a, b, c)(10.millis)) }
            .forall(_ == Some(1))
        )
      }
    }

  }


  // Exception classes for testing failures
  class E1(msg: String) extends Exception(msg)
  class E2(msg: String) extends Exception(msg)
}
