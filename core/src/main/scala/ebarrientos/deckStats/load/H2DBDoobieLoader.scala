package ebarrientos.deckStats.load

import _root_.ebarrientos.deckStats.config.CoreConfig
import _root_.ebarrientos.deckStats.basics.Card

import cats._, cats.implicits._, cats.effect._

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import zio.Task
import zio.interop.catz._
import zio.console._
import ebarrientos.deckStats.load.utils.LoadUtils

import ebarrientos.deckStats.stringParsing.MtgJsonParser.{parseAll, cost, stringify}
import ebarrientos.deckStats.basics.Supertype
import ebarrientos.deckStats.basics.CardType
import scala.concurrent.ExecutionContext
import org.slf4j.LoggerFactory

class H2DBDoobieLoader(val helper: CardLoader, config: CoreConfig, ec: ExecutionContext)
    extends CardLoader with StoringLoader with LoadUtils
{
  val log = LoggerFactory.getLogger(getClass())

  val xa = Transactor.fromDriverManager[Task](
    config.dbDriver,        // driver classname
    config.dbConnectionUrl, // connect URL (driver-specific)
    "",                     // user
    "",                     // password
    Blocker.liftExecutionContext(ec)
  )

  val cons: Console.Service = zio.console.Console.Service.live


  override protected def retrieve(name: String): Task[Option[Card]] =
    for {
      ocard <- queryCard(name).transact(xa)
    } yield toCard(ocard)


  override protected def store(c: Card): Task[Unit] =
    for (_ <- storeCard(c).run.transact(xa)) yield ()


  private def queryCard(name: String) = {
    log.debug("Querying for cardName = {}", name)
    sql"""|select name, cost, typeline, text, power, toughness
          |from cards
          |where name = $name"""
      .stripMargin
      .query[(String, String, String, String, Int, Int)].option
  }

  /** Card from query result */
  private def toCard(oc: Option[(String, String, String, String, Int, Int)]): Option[Card] =
    oc map { case (name, manaCost, typeLine, text, power, toughness) =>
      val (supertypes, types, subTypes) = parseTypes(typeLine)
      val parsedCost = parseAll(cost, manaCost).get
      Card(parsedCost, name, types, supertypes, subTypes, text, power, toughness)
    }

  /** Query to store a card in the DB
    *
    * @param c The {{Card}}
    */
  private def storeCard(c: Card): Update0 =
    sql"""|INSERT INTO cards(name, cost, typeline, text, power, toughness)
          |VALUES (${c.name}, ${stringify(c.cost)}, ${mkTypeline(c)}, ${c.text},
          |        ${c.power}, ${c.toughness})"""
      .stripMargin
      .update


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

  /** Build a typeline from a card */
  private[this] def mkTypeline(c: Card): String =
    mkTypeline(c.supertypes, c.types, c.subtypes)

  private val cardsDDL =
    sql"""Create table IF NOT EXISTS cards (
            name VARCHAR(100) NOT NULL,
            cost VARCHAR(100),
            typeline VARCHAR(150),
            text VARCHAR(200),
            power INT,
            toughness INT);""".update.run

  /** Initialize the cards table for the database */
  def initTable() = cardsDDL.transact(xa)

  // Guarantee the cards table exists before anything else happens
  zio.Runtime.default.unsafeRun(initTable())
}

