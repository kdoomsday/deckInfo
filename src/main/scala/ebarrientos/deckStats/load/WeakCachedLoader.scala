package ebarrientos.deckStats.load

import scala.collection.mutable.WeakHashMap
import ebarrientos.deckStats.basics.Card

/** Cached loader that stores cards in a weak hash map, to prevent too much growth. */
class WeakCachedLoader(val helper: CardLoader) extends CardLoader {
  private[this] lazy val map = new WeakHashMap[String, Card]

  def card(name: String): Option[Card] = {
    if (map.contains(name)) Some(map(name))
    else {
      val c = helper.card(name)
      c foreach(card => map(name) = card)
      c
    }
  }
}
