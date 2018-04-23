package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import scala.collection.mutable.HashMap

/** Loader that caches values in memory for repeated use. */
class CachedLoader(private val l: CardLoader) extends CardLoader {
  private[this] val map = mutable.HashMap[String, Card]()


  def card(name: String): Option[Card] =
    if (map contains name) map.get(name)
    else {
      val oCard = l.card(name)
      oCard.foreach(map(name) = _)
      oCard
    }
}
