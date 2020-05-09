package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.IO

/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader]) extends CardLoader {
  def this(ls: CardLoader*) = this(ls.toList)

  def card(name: String): IO[Throwable, Option[Card]] = {
    def loop(ls: List[CardLoader]): IO[Throwable, Option[Card]] = ls match {
      case Nil => IO.succeed(None)
      case loader :: rest =>
        for {
          oc <- loader.card(name)
          res <- if (oc.isDefined) IO.succeed(oc) else loop(rest)
        } yield res
    }

    loop(loaders)
  }
}
