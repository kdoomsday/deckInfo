package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.*
import ebarrientos.deckStats.basics.Color.*
import zio.IO
import utest.*

import scala.io.Source
import ebarrientos.deckStats.basics.CardType.*

object MtgJsonLoaderTest extends TestSuite {
  private val text =
    Source.fromInputStream(
      getClass
        .getClassLoader
        .getResourceAsStream("Sample.json")
    ).mkString

  val loader = new MtgJsonLoader(text)

  val tests = Tests {
    "Load a creature from the sample" - {
      val ioCard: IO[Exception, Option[Card]] = loader.card("Adorable Kitten")
      for ( maybeCard <- ioCard ) yield {
        assert(maybeCard.isDefined)
        maybeCard.fold(assert(false)){ (kitten: Card) =>
        assert(kitten.name == "Adorable Kitten",
               kitten.cost == Seq(ColoredMana(White)),
               kitten.subtypes contains "Cat",
               kitten.types contains Creature,
               kitten.power == 1,
               kitten.toughness == 1)
        }
      }
    }

    "Load a sorcery" - {
      val ioWrath: IO[Exception, Option[Card]] = loader.card("Wrath of God")
      for ( maybeWrath <- ioWrath ) yield {
        assert(maybeWrath.isDefined)
        maybeWrath.fold(assert(false)) { wrath =>
          assert(
            wrath.cmc == 4,
            wrath.is(White),
            wrath.types contains Sorcery
          )
        }
      }
    }

    "Land" - {
      for (maybePlains <- loader.card("Plains") ) yield {
        maybePlains.fold(assert(false)) { plains =>
          assert(
            plains.cmc == 0,
            plains.supertypes contains Supertype.Basic,
            plains.types contains Land
          )
        }
      }
    }
  }
}
