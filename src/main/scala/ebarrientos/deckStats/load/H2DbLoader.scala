package ebarrientos.deckStats.load

import ebarrientos.deckStats.db.DBInfo._
import scala.slick.driver.H2Driver.simple._
import ebarrientos.deckStats.basics.Card
import scala.slick.jdbc.meta.MTable

/** Loads a card from DB. If db doesn't contain the card, uses the helper to load and store it for
  * future retrieval.
  */
class H2DbLoader(val helper: CardLoader) extends CardLoader {
  val db = Database.forURL("jdbc:h2:cards", driver="org.h2.Driver")

  // Check for existence of table, and create if necessary
  db.withSession { implicit session =>
    if (MTable.getTables("Cards").list.isEmpty) {
      println("Creating Cards table...")
      cards.ddl.create
    }
  }


  def card(name: String): Card = db.withSession { implicit session =>
    val c = cards.filter(_.name === name).firstOption

    c match {
      case Some(card) => card
      case None => {
        val loaded = helper.card(name)
        cards += loaded
        loaded
      }
    }
  }
}