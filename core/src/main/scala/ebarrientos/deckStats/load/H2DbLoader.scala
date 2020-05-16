package ebarrientos.deckStats.load

import ebarrientos.deckStats.db.DBInfo._

import scala.slick.driver.H2Driver.simple._
import ebarrientos.deckStats.basics.Card

import scala.slick.driver.H2Driver
import scala.slick.jdbc.meta.MTable
import zio.IO
import ebarrientos.deckStats.config.CoreConfig

/** Loads a card from DB. If db doesn't contain the card, uses the helper to load and store it for
  * future retrieval.
  */
class H2DbLoader(val helper: CardLoader, config: CoreConfig) extends CardLoader  with StoringLoader {
  def this(config: CoreConfig) = this(NullCardLoader, config)
  def this() = this(CoreConfig("jdbc:h2:cards", "org.h2.Driver"))

  val db: H2Driver.backend.DatabaseDef = Database.forURL(config.dbConnectionUrl, driver=config.dbDriver)

  // Check for existence of table, and create if necessary
  db.withSession { implicit session =>
    if (MTable.getTables("Cards").list.isEmpty) {
      println("Creating Cards table...")
      cards.ddl.create
    }
  }

  protected def retrieve(name: String): Option[Card] = db.withSession { implicit session =>
    cards.filter(_.name === name).firstOption
  }

  protected def store(c: Card): IO[Exception, Unit] = db.withSession { implicit session =>
    IO.succeed(cards += c)
  }


  // def card(name: String): Option[Card] = db.withSession { implicit session =>
  //   val c = cards.filter(_.name === name).firstOption

  //   c match {
  //     case Some(card) => Some(card)
  //     case None => {
  //       val loaded = helper.card(name)
  //       loaded.foreach { c => cards += c }
  //       loaded
  //     }
  //   }
  // }
}
