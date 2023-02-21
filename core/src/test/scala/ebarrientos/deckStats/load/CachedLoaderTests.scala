package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.DummyObjects.arthur
import zio.ZIO
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers._
import ebarrientos.deckStats.TestHelper.run

/**
 * Tests for the CachedLoader
 */
object CachedLoaderTests extends TestSuite with MockitoSugar {

  val tests = Tests {
    test("caches the result") {
      val c = mock[CardLoader]
      val name = arthur.name
      when(c.card(name)).thenReturn(ZIO.some(arthur))

      val cachedLoader = new CachedLoader(c)

      val card = cachedLoader.card(name)
      assert(run(card) == Some(arthur))

      val card2 = cachedLoader.card(name)
      assert(run(card2) == Some(arthur))

      verify(c, times(1)).card(any)
    }
  }
}

