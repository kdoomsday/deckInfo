package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import zio._

/** Loader that caches values in memory for repeated use. */
class CachedLoader(private val l: CardLoader) extends CardLoader {
  private[this] val map = mutable.HashMap[String, Card]()

  def card(name: String): Task[Option[Card]] =
    if (map contains name) ZIO.succeed(map.get(name))
    else {
      for {
        oc <- l.card(name)
      } yield {
        oc.foreach(c => map(name) = c)
        oc
      }
    }
}
