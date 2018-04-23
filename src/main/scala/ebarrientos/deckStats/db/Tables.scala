package ebarrientos.deckStats.db

import ebarrientos.deckStats.basics.{Card, CardType, Supertype}
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.stringParsing.ManaParser

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.TableQuery

object DBInfo {

  /** Table that stores the cards. */
  class Cards(tag: Tag) extends Table[Card](tag, "Cards") {
    def name:      Column[String] = column("name", O.PrimaryKey)
    def cost:      Column[String] = column("cost", O.NotNull)
    def typeline:  Column[String] = column("typeline", O.NotNull)
    def text:      Column[String] = column("text")
    def power:     Column[Int]    = column("power")
    def toughness: Column[Int]    = column("toughness")

    def * = (name, cost, typeline, text,power, toughness) <> (mkCard, unappCard _)


    private[this] val parser = ManaParser
    private[this] lazy val load = new AnyRef() with LoadUtils


    /** Build a card from it's components */
    private[this] val mkCard =
      (info: (String, String, String, String, Int, Int))
        => info match { case (name, cost, typeline, text, power, toughness) =>
          val (supertypes, types, subtypes) = load.parseTypes(typeline)
          val parsedCost = parser.parseAll(parser.cost, cost).get
          Card(parsedCost, name, types, supertypes, subtypes, text, power, toughness)
        }

    /** Build a typeline from it's component information */
    private[this] def mkTypeline(
        supertypes: Set[Supertype],
        types: Set[CardType],
        subtypes: Set[String]): String =
    {
      val t = (supertypes ++ types).mkString(" ")
      val res = if ((subtypes eq null) || subtypes.isEmpty) t
                else t + " - " + subtypes.mkString(" ")

      res
    }

    def unappCard(c: Card) =
      Some(c.name, c.cost.mkString, mkTypeline(c.supertypes, c.types, c.subtypes), c.text, c.power, c.toughness)

  }
  def cards: TableQuery[Cards] = TableQuery[Cards]
}