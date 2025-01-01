package ebarrientos.deckStats.load.card

import java.io.File
import utest._
import ebarrientos.deckStats.TestHelper
import ebarrientos.deckStats.basics.CardType.Enchantment

object XMLCardLoaderTests extends TestSuite {

  val fileOldFormat = new File(getClass().getClassLoader().getResource("cardsTest_oldFormat.xml").getFile())
  val fileNewFormat = new File(getClass().getClassLoader().getResource("cardsTest_newFormat.xml").getFile())

  @inline def withLoader(file: File) = new XMLCardLoader(file.getAbsolutePath())
  @inline def withOldLoader() = withLoader(fileOldFormat)
  @inline def withNewLoader() = withLoader(fileNewFormat)

  val tests = Tests {

    test("load known card") {
      val loader = withNewLoader()
      val res = TestHelper.run(loader.card("Etchings of the Chosen"))
      assert(res.isDefined)

      val card = res.get
      assert(card.cmc == 3)
      assert(card.types.contains(Enchantment))
      assert(card.multiverseId == Some(464147))
      card.name
    }

    test("load known card in old format") {
      val loader = withOldLoader()
      val res = TestHelper.run(loader.card("Tundra Kavu"))
      assert(res.isDefined)

      val card = res.get
      assert(card.power == 2)
      assert(card.toughness == 2)
      assert(card.cmc == 3)
      assert(card.multiverseId == None)
      card.name
    }

    test("load nonexistent card in new format") {
      val loader = withNewLoader()
      val res = TestHelper.run(loader.card("Eduardo's egregious card name"))
      assert(res.isEmpty)
    }

    test("load nonexistent card in old format") {
      val loader = withOldLoader()
      val res = TestHelper.run(loader.card("Eduardo's egregious card name"))
      assert(res.isEmpty)
    }

    test("load new format deathrite") {
      val loader = withNewLoader()
      val res = TestHelper.run(loader.card("Deathrite Shaman"))
      val card = res.get

      assert(card.power == 1)
      assert(card.toughness == 2)
      assert(card.multiverseId.isDefined)
      assert(card.cmc == 1)
    }
  }
}
