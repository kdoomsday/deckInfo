package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import zio._

/** Cached loader that stores cards in a weak hash map, to prevent too much growth. */
class WeakCachedLoader(val helper: CardLoader) extends CardLoader {
  private[this] lazy val map = new mutable.WeakHashMap[String, Card]

  def card(name: String): Task[Option[Card]] =
    if (map.contains(name))
      ZIO.succeed(map.get(name))
    else
      for {
        oCard <- helper.card(name)
        _     <- ZIO.succeed(store(name, oCard))
      } yield oCard

  // Guardar la carta, si existe
  private[this] def store(name: String, oCard: Option[Card]): Unit =
    oCard.foreach(card => map(name) = card)
}
