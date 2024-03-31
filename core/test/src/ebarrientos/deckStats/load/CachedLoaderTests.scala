package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.DummyObjects.arthur
import zio.ZIO
import ebarrientos.deckStats.TestHelper.run
import eu.monniot.scala3mock.ScalaMocks.*

/**
 * Tests for the CachedLoader
 */
object CachedLoaderTests extends TestSuite {

  val tests = Tests {
    test("caches the result") {
      withExpectations() {
        val c = mock[CardLoader]
        val name = arthur.name
        // when(c.card(name)).thenReturn(ZIO.some(arthur))
        when(c.card)
          .expects(name)
          .returns(ZIO.some(arthur))
          .once

        val cachedLoader = new CachedLoader(c)

        val card = cachedLoader.card(name)
        assert(run(card) == Some(arthur))

        val card2 = cachedLoader.card(name)
        // verify(c, times(1)).card(any)
        assert(run(card2) == Some(arthur))
      }
    }
  }
}

