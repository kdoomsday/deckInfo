package ebarrientos.deckStats.load.card


import ebarrientos.deckStats.basics.Card
import zio._

import SequenceLoader.defaultDelay


/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader])(nextStartDelay: Duration = defaultDelay)
    extends CardLoader {
  def this(ls: CardLoader*)(nextStartDelay: Duration) = this(ls.toList)(nextStartDelay)
  def this(ls: CardLoader*) = this(ls.toList)(defaultDelay)


  def card(name: String): Task[Option[Card]] = loaders match {
    case Nil     => ZIO.succeed(None)
    case l :: ls =>
      ZIO
        .raceAll(
          l.card(name).someOrFailException,
          ls.zipWithIndex.map { case (loader, i) =>
            ZIO.sleep(nextStartDelay * (i + 1)) *> loader.card(name).someOrFailException
          }
        )
        .map(Option.apply)
        .catchSome { case _: NoSuchElementException => ZIO.succeed(None) }
  }

}


object SequenceLoader:
  val defaultDelay: java.time.Duration = 500.millis
