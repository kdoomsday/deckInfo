package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics._
import utest._

import scala.io.Source

object MTGJsonLoaderTest extends TestSuite {
  val tests = Tests {
    val text =
      Source.fromInputStream(
        getClass
          .getClassLoader
          .getResourceAsStream("Sample.json")
      ).mkString

    val loader = new MtgJsonLoader(text)

    "Load a card from the sample" - {
      val maybeCard = loader.card("Adorable Kitten")
      assert(!maybeCard.isEmpty)
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
  }
}
