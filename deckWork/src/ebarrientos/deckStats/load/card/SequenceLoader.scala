package ebarrientos.deckStats.load.card

import ebarrientos.deckStats.basics.Card
import zio._

/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader]) extends CardLoader {
  def this(ls: CardLoader*) = this(ls.toList)

  def card(name: String): Task[Option[Card]] = {
    def loop(ls: List[CardLoader]): Task[Option[Card]] = ls match {
      case Nil => ZIO.succeed(None)
      case loader :: rest =>
        for {
          oc <- loader.card(name)
          res <- if (oc.isDefined) ZIO.succeed(oc) else loop(rest)
        } yield res
    }

    loop(loaders)
  }
}
