package ebarrientos.deckStats.load

import ebarrientos.deckStats.db.DBInfo._

import scala.slick.driver.H2Driver.simple._
import ebarrientos.deckStats.basics.Card

import scala.slick.driver.H2Driver
import scala.slick.jdbc.meta.MTable
import scalaz.zio.IO

/** Loads a card from DB. If db doesn't contain the card, uses the helper to load and store it for
  * future retrieval.
  */
class H2DbLoader(val helper: CardLoader) extends CardLoader  with StoringLoader {
  def this() = this(NullCardLoader)

  val db: H2Driver.backend.DatabaseDef = Database.forURL("jdbc:h2:cards", driver="org.h2.Driver")

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
    IO.sync(cards += c)
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
