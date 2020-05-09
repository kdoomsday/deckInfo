package ebarrientos.deckStats.load

import scala.collection.mutable.WeakHashMap
import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import zio.IO

/** Cached loader that stores cards in a weak hash map, to prevent too much growth. */
class WeakCachedLoader(val helper: CardLoader) extends CardLoader {
  private[this] lazy val map = new mutable.WeakHashMap[String, Card]

  def card(name: String): IO[Throwable, Option[Card]] =
    if (map.contains(name))
      IO.succeed(map.get(name))
    else
      for {
        oCard <- helper.card(name)
        _     <- IO.succeed(store(name, oCard))
      } yield oCard

  // Guardar la carta, si existe
  private[this] def store(name: String, oCard: Option[Card]): Unit =
    oCard.foreach(card => map(name) = card)
}
