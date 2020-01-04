package ebarrientos.deckStats.math

/** Seq operations. */
object Seqs {

  /** Return a map with the number of appearances of each item in a Seq */
  def encode[T](s: Seq[T]): Map[T, Int] = {
    def enc(seq: Seq[T], map: Map[T, Int]): Map[T, Int] = seq match {
      case some if some.isEmpty => map
      case ss => enc(ss.tail, map updated (ss.head, map(ss.head)+1))
    }

    enc(s, Map[T, Int]().withDefaultValue(0))
  }

  def newEncode[T](s: Seq[T]): Map[T, Int] =
    s.groupBy(e => e).map{ case (e, l) => (e, l.size) }
}
