package ebarrientos.deckStats.load.card


import utest._
import zio._
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.TestHelper
import ebarrientos.deckStats.load.DummyObjects
import org.scalamock.stubs.Stubs
import org.scalamock.stubs.Stub
import java.time.Duration


object RacingLoaderTest extends TestSuite, Stubs {

  /** Loader that never finds a card */
  private def emptyLoader: Stub[CardLoader] =
      val res = stub[CardLoader]
      res.card.returnsWith(ZIO.succeed(None))
      res


  /** Loader that always finds a specific card */
  private def loaderFinds(found: Card): Stub[CardLoader] =
      val res = stub[CardLoader]
      res.card.returnsWith(ZIO.succeed(Some(found)))
      res


  /** Loader that finds a card, but only after a delay */
  private def loaderDelay(found: Card, delay: Duration) =
      val res = stub[CardLoader]
      res.card.returnsWith(ZIO.sleep(delay) *> ZIO.succeed(Some(found)))
      res


  val tests = Tests {
    test("all loaders None => None") {
      val l1     = emptyLoader
      val l2     = emptyLoader
      val loader = new RacingLoader(l1, l2)(20.millis)

      val res = TestHelper.run(loader.card("foo"))
      assert(res.isEmpty)
      assert(l1.card.calls == List("foo"))
    }

    test("returns from first loader regardless of second") {
      val l1     = loaderDelay(DummyObjects.arthur, 10.millis)
      val l2     = loaderFinds(DummyObjects.trillian)
      val loader = new RacingLoader(l1, l2)(50.millis)

      val name = DummyObjects.arthur.name
      val res  = TestHelper.run(loader.card(name))
      assert(res.isDefined)
      assert(res == Option(DummyObjects.arthur))
      assert(l1.card.calls == List(name))
    }

    test("returns from faster staggered loader") {
      val l1     = loaderDelay(DummyObjects.arthur, 100.millis)
      val l2     = loaderDelay(DummyObjects.trillian, 10.millis)
      val loader = new RacingLoader(l1, l2)(20.millis)

      val res = TestHelper.run(loader.card("any"))
      assert(res == Option(DummyObjects.trillian))
      assert(l1.card.times == 1)
      assert(l2.card.times == 1)
    }

    test("first failing loader does not fail the task") {
      val l1     = new CardLoader {
        def card(name: String): Task[Option[Card]] = ZIO.fail(new RuntimeException("boom"))
      }
      val l2     = loaderFinds(DummyObjects.trillian)
      val loader = new RacingLoader(l1, l2)(50.millis)

      val res = TestHelper.run(loader.card("err"))
      assert(res == Option(DummyObjects.trillian))
    }

    test("single loader failing fails the task") {
      val message = "boom"
      val l1      = new CardLoader {
        def card(name: String): Task[Option[Card]] = ZIO.fail(new RuntimeException(message))
      }
      val loader  = new RacingLoader(l1)(50.millis)

      val res = assertThrows[RuntimeException] {
        TestHelper.run(loader.card("err"))
      }
      assert(res.getMessage() == message)
    }

    test("all loaders failing fails the task") {
      val message = "boom"
      val l1      = new CardLoader {
        def card(name: String): Task[Option[Card]] = ZIO.fail(new RuntimeException(message))
      }
      val loader  = new RacingLoader(l1, l1)(50.millis)

      val res = assertThrows[RuntimeException] {
        TestHelper.run(loader.card("err"))
      }
      assert(res.getMessage() == message)
    }

    test("when loader returns None next starts immediately") {
      val start  = java.lang.System.currentTimeMillis()
      val l1     = stub[CardLoader]
      l1.card.returnsWith(ZIO.succeed(None))
      val l2     = loaderDelay(DummyObjects.arthur, 50.millis)
      val delay  = 500.millis
      val loader = new RacingLoader(l1, l2)(delay)

      val res     = TestHelper.run(loader.card("any"))
      assert(res == Option(DummyObjects.arthur))
      val elapsed = (java.lang.System.currentTimeMillis() - start).millis
      assert(elapsed < delay)
    }

    test("next loader is started only once even if previous returns None") {
      val run = Ref.make(0).flatMap { runs =>
          val l1     = ZIO.succeed(None)
          val l2     = runs.getAndUpdate(_ + 1) *> ZIO.sleep(50.millis) *> ZIO.some(DummyObjects.arthur)
          val loader = new RacingLoader(
            new CardLoader {
              def card(name: String) = l1
            },
            new CardLoader {
              def card(name: String) = l2
            }
          )(25.millis)
          (loader.card("any")).zip(runs.get)
      }

      val (res, runs) = TestHelper.run(run)
      assert(res == Some(DummyObjects.arthur))
      assert(runs == 1)
    }

    test("three loaders: staggered starts and first to succeed wins") {
      val l1     = loaderDelay(DummyObjects.arthur, 100.millis)
      val l2     = loaderDelay(DummyObjects.trillian, 80.millis)
      val l3     = loaderDelay(DummyObjects.marvin, 20.millis)
      val loader = new RacingLoader(l1, l2, l3)(30.millis)

      val res = TestHelper.run(loader.card("any"))
      assert(res.contains(DummyObjects.marvin)) // Loader 3's value
      assert(l1.card.times == 1)
      assert(l2.card.times == 1)
      assert(l3.card.times == 1)
    }
  }

}
