package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scalaz.zio.IO

/** Loader that caches values in memory for repeated use. */
class CachedLoader(private val l: CardLoader) extends CardLoader {
  private[this] val map = mutable.HashMap[String, Card]()


  def card(name: String): IO[Exception, Option[Card]] =
    if (map contains name) IO.point(map.get(name))
    else {
      val ioCard: IO[Exception, Option[Card]] = l.card(name)
      for {
        oc <- ioCard
      } yield {
        for (c <- oc) (map(name) = c)
        oc
      }
    }
}
