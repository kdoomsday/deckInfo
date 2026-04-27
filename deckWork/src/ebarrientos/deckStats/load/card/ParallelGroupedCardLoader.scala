package ebarrientos.deckStats.load.card


import ebarrientos.deckStats.basics.Card
import zio.ZIO


/**
 * Card loader that, when loading multiple cards, does so by doing it in
 * parallel, but up to a maximum at a time.
 * Useful to get a free implementation of `cards` that runs in parallel
 * bounded, if there is no better way
 */
trait ParallelGroupedCardLoader extends CardLoader {
  def maxParallelExecutions: Int


  override def cards(names: Seq[String]): ZIO[Any, Throwable, Seq[Card]] = {
    val allExecs = names
      .grouped(maxParallelExecutions)
      .map(group => ZIO.collectAllSuccessesPar(group.map(c => card(c))))
    ZIO
      .collectAll(allExecs.toSeq)
      .map(_.map(_.flatten).flatten)
  }

}
