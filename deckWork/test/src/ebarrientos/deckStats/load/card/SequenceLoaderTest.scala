package ebarrientos.deckStats.load.card


import utest._
import zio._
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.TestHelper
import ebarrientos.deckStats.load.DummyObjects
import org.scalamock.stubs.Stubs
import org.scalamock.stubs.Stub


object SequenceLoaderTest extends TestSuite, Stubs {

  private val runtime = Runtime.default


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
      val loader = new SequenceLoader(l1, l2)

      val res = TestHelper.run(loader.card("foo"))
      assert(res.isEmpty)
    }

    test("returns from fisrt loader regardless of second") {
      val l1     = loaderFinds(DummyObjects.arthur)
      val l2     = loaderFinds(DummyObjects.trillian)
      val loader = new SequenceLoader(l1, l2)

      val name = DummyObjects.arthur.name
      val res  = TestHelper.run(loader.card(name))
      assert(res.isDefined)
      assert(res == Option(DummyObjects.arthur))
      assert(l1.card.calls == List(name))
      assert(l2.card.times == 0)
    }

    test("returns from second loader") {
      val l1     = emptyLoader
      val l2     = loaderFinds(DummyObjects.arthur)
      val loader = new SequenceLoader(l1, l2)

      val name = DummyObjects.arthur.name
      val res  = TestHelper.run(loader.card(name))
      assert(res.isDefined)
      assert(l1.card.calls == List(name))
      assert(l2.card.calls == List(name))
      assert(res == Option(DummyObjects.arthur))
    }

    test("one failing loader does not fail the task") {
      val l1      = new CardLoader {
        def card(name: String): Task[Option[Card]] = ZIO.fail(new RuntimeException("boom"))
      }
      val l2      = loaderFinds(DummyObjects.trillian)
      val loader  = new SequenceLoader(l1, l2)

      val res = TestHelper.run(loader.card("err"))
      assert(res == Option(DummyObjects.trillian))
    }

    test("all loaders failing fails the taks") {
      val message = "boom"
      val l1      = new CardLoader {
        def card(name: String): Task[Option[Card]] = ZIO.fail(new RuntimeException(message))
      }
      val loader  = new SequenceLoader(l1)

      val res = intercept[RuntimeException] {
        TestHelper.run(loader.card("err"))
      }
      assert(res.getMessage() == message)
    }

    test("loaders are raced, fastest result gets picked up") {
      val l1 = loaderDelay(DummyObjects.arthur, 100.millis)
      val l2 = loaderDelay(DummyObjects.trillian, 10.millis)
      val loader = SequenceLoader(l1, l2)(20.millis)

      val res = TestHelper.run(loader.card("any"))
      assert(res == Option(DummyObjects.trillian))
      assert(l1.card.times == 1)
      assert(l2.card.times == 1)
    }
  }

}
