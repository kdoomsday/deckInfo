package ebarrientos.deckStats.stringParsing

import ebarrientos.deckStats.basics._
import utest._

object MtgJsonParserTest extends TestSuite {
  val p: String => Seq[Mana] =
    (s: String) => MtgJsonParser.parseAll(MtgJsonParser.cost, s).get

  val tests = Tests {
    def assertMana(expected: Mana, actual: Seq[Mana]): Unit = {
      assert(actual.size == 1)
      val mana = actual.head
      assert(expected == mana)
    }

    def assertManaList(expected: Seq[Mana], actual: Seq[Mana]): Unit = {
      assert(expected.size == actual.size)
      assert(
        expected.map(_.toString)
                .sorted
                .zip(actual.map(_.toString).sorted)
                .forall { case (x, y) => x == y }
      )
    }

    "Parse colored mana" - {
      * - assertMana(ColoredMana(White), p("{W}"))
      * - assertMana(ColoredMana(Blue),  p("{U}"))
      * - assertMana(ColoredMana(Black), p("{B}"))
      * - assertMana(ColoredMana(Red),   p("{R}"))
      * - assertMana(ColoredMana(Green), p("{G}"))
    }

    "Generic mana" - {
      * - assertMana(GenericMana(0),  p("{0}"))
      * - assertMana(GenericMana(1),  p("{1}"))
      * - assertMana(GenericMana(3),  p("{3}"))
      * - assertMana(GenericMana(16), p("{16}"))
    }

    "Hybrid mana" - {
      "colors" - assertMana(HybridMana(Set(ColoredMana(Black), ColoredMana(Green))), p("{B/G}"))
      "mixed"  - assertMana(HybridMana(Set(ColoredMana(White), GenericMana(2))),   p("{W/2}"))
    }

    "Phyrexian mana" - {
      val smana = p("{B/P}")
      assert(smana.size == 1)
      val mana = smana.head
      assert(
        mana.is(Black),
        mana.hasProperty(Phyrexian)
      )
    }

    "Multiple mana" - {
      * - assertManaList(Seq(ColoredMana(Black), GenericMana(1)), p("{1}{B}"))
      * - assertManaList(Seq(GenericMana(1), ColoredMana(Black)), p("{1}{B}"))
      * - assertManaList(Seq(ColoredMana(White), ColoredMana(Blue)), p("{W}{U}"))
    }

    "Snow mana" - {
      * - assertManaList(Seq(GenericMana(1, Set(SnowMana))), p("{S}"))
    }
  }
}
