package ebarrientos.deckStats.queries

/** Response object to a deck query
  *
  * @param avgCMC         Full deck avg mana cost
  * @param avgCMCNonLands Avg mana cost not counting lands
  * @param counts         [[TypeCounts]] with card type counts for the deck
  */
case class DeckObject(
    avgCMC        : Double,
    avgCMCNonLands: Double,
    counts        : Seq[CountObject],
    manaSymbols   : Seq[CountObject],
    manaCurve     : Seq[CurvePoint]
)

/** Encapsulates a category and it's count
  *
  * @param name  Category name
  * @param count Count
  */
case class CountObject(name: String, count: Double)

/** A point in a (mana)curve */
case class CurvePoint(cost: Int, amount: Int)

object CurvePoint {

  /** CurvePoint from a Tuple2
    *
    * @param cp Tuple2 as (cost, amount)
    * @return A CurvePoint from the data
    */
  def apply(cp: Tuple2[Int, Int]): CurvePoint = new CurvePoint(cp._1, cp._2)
}
