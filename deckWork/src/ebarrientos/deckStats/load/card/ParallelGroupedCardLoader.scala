package ebarrientos.deckStats.load.card

import ebarrientos.deckStats.basics.Card
import zio.ZIO

trait ParallelGroupedCardLoader extends CardLoader {
  def maxParallelExecutions: Int

  override def cards(names: Seq[String]): ZIO[Any,Throwable,Seq[Card]] = {
    val allExecs = names.grouped(maxParallelExecutions).map(group => ZIO.collectAllPar(group.map(c => card(c))))
    ZIO.collectAll(allExecs.toSeq)
      .map(_.map(_.flatten).flatten)
  }
}
