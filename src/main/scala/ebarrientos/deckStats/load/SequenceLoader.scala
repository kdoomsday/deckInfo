package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import scalaz.zio.IO

/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader]) extends CardLoader {
  def this(ls: CardLoader*) = this(ls.toList)

  def card(name: String): IO[Exception, Option[Card]] = {
    def loop(ls: List[CardLoader]): IO[Exception, Option[Card]] = ls match {
      case Nil => IO.point(None)
      case loader :: rest =>
        for {
          oc <- loader.card(name)
          res <- if (oc.isDefined) IO.point(oc) else loop(rest)
        } yield res
    }

    loop(loaders)
  }
}
