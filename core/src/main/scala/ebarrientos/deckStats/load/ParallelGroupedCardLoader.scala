package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import zio.ZIO
import zio.Chunk

trait ParallelGroupedCardLoader extends CardLoader {
  def maxParallelExecutions: Int

  override def cards(names: Seq[String]): ZIO[Any,Throwable,Seq[Card]] = {
    val allExecs = names.grouped(maxParallelExecutions).map(group => ZIO.collectAllPar(group.map(c => card(c))))
    val res = ZIO.collectAll(allExecs.toSeq)

    res.map(_.map(_.flatten).flatten)
  }
}
