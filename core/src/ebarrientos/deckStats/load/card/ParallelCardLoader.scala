package ebarrientos.deckStats.load.card

import ebarrientos.deckStats.basics.Card
import zio.ZIO

trait ParallelCardLoader extends CardLoader {

  override def cards(names: Seq[String]): ZIO[Any,Throwable,Seq[Card]] =
    ZIO.collectAllPar(names.map(name => card(name))).map(_.flatten)
}

