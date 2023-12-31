package ebarrientos.deckStats.queries

/**
 * Response object to a deck query
 *
 * @param avgCMC         Full deck avg mana cost
 * @param avgCMCNonLands Avg mana cost not counting lands
 * @param cardCount      Number of cards in the deck
 * @param counts         [[TypeCounts]] with card type counts for the deck
 * @param manaSymbols    Mana symbol counts
 * @param manaCurve      Points to create a Mana Curve
 * @param deckName       Name of the deck
 * @param cards          Names of cards in the deck, with counts
 */
case class DeckObject(
    avgCMC        : Double,
    avgCMCNonLands: Double,
    cardCount     : Int,
    counts        : Seq[CountObject],
    manaSymbols   : Seq[CountObject],
    manaCurve     : Seq[CurvePoint],
    deckName      : String,
    cards         : Seq[CardObject]
)

/** Encapsulates a category and it's count
  *
  * @param name  Category name
  * @param count Count
  */
case class CountObject(name: String, count: Double)

/**
 * Card info for the response
 *
 * @param name Card name
 * @param count How many are present in the deck
 * @param multiverseId Card multiverse id
 */
case class CardObject(name: String, count: Double, multiverseId: Int)

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
