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

import ebarrientos.deckStats.stringParsing.MagicApiManaParser.{parseAll, cost}
import ebarrientos.deckStats.basics.Supertype
import ebarrientos.deckStats.basics.CardType

class H2DBDoobieLoader(val helper: CardLoader, config: CoreConfig)
    extends CardLoader with StoringLoader with LoadUtils
{
  val xa = Transactor.fromDriverManager[Task](
    config.dbDriver,     // driver classname
    config.dbConnectionUrl,     // connect URL (driver-specific)
    "",                  // user
    "",                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  val cons: Console.Service = zio.console.Console.Service.live

  override protected def retrieve(name: String): Task[Option[Card]] =
    // cardsDDL.flatMap(_ => queryCard(name)).transact(xa).map(toCard _)
    for {
      _     <- cardsDDL.transact(xa)
      _     <- cons.putStrLn(s"DDL created, searching for $name")
      ocard <- queryCard(name).transact(xa)
      res    = toCard(ocard)
      _     <- cons.putStrLn(s"Card queried: $res")
    } yield res

  override protected def store(c: Card): Task[Unit] =
    for (_ <- storeCard(c).run.transact(xa)) yield ()

  private def queryCard(name: String) =
    sql"""
      select name, cost, typeline, text, power, toughness
      from CARDS
      where name = $name
    """.query[(String, String, String, String, Int, Int)].option

  /** Card from query result */
  private def toCard(oc: Option[(String, String, String, String, Int, Int)]): Option[Card] =
    oc map { case (name, manaCost, typeLine, text, power, toughness) =>
      val (supertypes, types, subTypes) = parseTypes(typeLine)
      val parsedCost = parseAll(cost, manaCost).get
      Card(parsedCost, name, types, supertypes, subTypes, text, power, toughness)
    }

  private def storeCard(c: Card) =
    sql"""
      insert into Cards(name, cost, typeline, text, power, toughness)
      values (${c.name}, ${c.cost.mkString}, ${mkTypeline(c)}, ${c.text}, ${c.power}, ${c.toughness})
    """.update


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
    sql"""|Create table IF NOT EXISTS CARDS(
          |  name varchar(100) NOT NULL,
          |  cost varchar(100),
          |  typeline varchar(150),
          |  text varchar(200),
          |  power int,
          |  toughness int);
    """.stripMargin.update.run

  // Crear la tabla inicialmente si no existe
  // zio.Runtime.default.unsafeRunSync {
  //   for {
  //     _ <- putStr("Initializing DDL")
  //     _ <- cardsDDL.transact(xa)
  //     _ <- putStr("Done initializing DDL")
  //   } yield ()
  // }
}

