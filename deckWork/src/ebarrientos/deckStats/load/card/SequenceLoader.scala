package ebarrientos.deckStats.load.card


import ebarrientos.deckStats.basics.Card
import zio._


/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader]) extends CardLoader {
  def this(ls: CardLoader*) = this(ls.toList)

  // def card(name: String): Task[Option[Card]] = {
  //   def loop(ls: List[CardLoader]): Task[Option[Card]] = ls match {
  //     case Nil            => ZIO.succeed(None)
  //     case loader :: rest =>
  //       for {
  //         oc  <- loader.card(name)
  //         res <- if (oc.isDefined) ZIO.succeed(oc) else loop(rest)
  //       } yield res
  //   }

  //   loop(loaders)
  // }


  def card(name: String): Task[Option[Card]] =
    if (loaders.isEmpty) ZIO.succeed(None)
    else
      raceWithDelays(loaders, 0, name)


  private def raceWithDelays(
      ls: List[CardLoader],
      initialDelay: Int,
      name: String
  ): Task[Option[Card]] = {
    val tasks = ls.zipWithIndex.map { case (loader, i) =>
      ZIO.sleep((initialDelay + i * 500).millis) *>
        loader.card(name).flatMap {
          case Some(c) => ZIO.succeed(c)
          case None    => ZIO.fail(new NoSuchElementException("Not found"))
        }
    }
    raceAll(tasks).map(Some(_)).catchSome { case _: NoSuchElementException => ZIO.succeed(None) }
  }


  private def raceAll(tasks: List[Task[Card]]): Task[Card] = tasks match {
    case t :: Nil  => t
    case t :: rest => t.race(raceAll(rest))
    case Nil       => ZIO.fail(new NoSuchElementException("No loaders"))
  }

}
