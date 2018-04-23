package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics._
import utest._

import scala.io.Source

object MTGJsonLoaderTest extends TestSuite {
  private val text =
    Source.fromInputStream(
      getClass
        .getClassLoader
        .getResourceAsStream("Sample.json")
    ).mkString

  val loader = new MtgJsonLoader(text)

  val tests = Tests {
    "Load a creature from the sample" - {
      val maybeCard = loader.card("Adorable Kitten")
      assert(maybeCard.isDefined)
      maybeCard.fold(assert(false)) { kitten =>
        assert(
          kitten.name == "Adorable Kitten",
          kitten.cost == Seq(ColoredMana(White)),
          kitten.subtypes contains "Cat",
          kitten.types contains Creature,
          kitten.power == 1,
          kitten.toughness == 1
        )
      }
    }

    "Load a sorcery" - {
      val maybeWrath = loader.card("Wrath of God")
      assert(maybeWrath.isDefined)
      maybeWrath.fold(assert(false)) { wrath =>
        assert(
          wrath.cmc == 4,
          wrath.is(White),
          wrath.types contains Sorcery
        )
      }
    }

    "Land" - {
      loader.card("Plains").fold(assert(false)) { plains =>
        assert(
          plains.cmc == 0,
          plains.supertypes contains Basic,
          plains.types contains Land
        )
      }
    }
  }
}
