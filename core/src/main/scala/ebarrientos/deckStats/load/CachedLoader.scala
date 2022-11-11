package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import scala.collection.mutable.HashMap
import zio._

/** Loader that caches values in memory for repeated use. */
class CachedLoader(private val l: CardLoader) extends CardLoader {
  private[this] val map = mutable.HashMap[String, Card]()


  def card(name: String): Task[Option[Card]] =
    if (map contains name) ZIO.succeed(map.get(name))
    else {
      val ioCard: Task[Option[Card]] = l.card(name)
      for {
        oc <- ioCard
      } yield {
        for (c <- oc) (map(name) = c)
        oc
      }
    }
}
