package ebarrientos.deckStats.math

import ebarrientos.deckStats.basics._
import models.DeckObject

/** Provides operations to apply to decks of cards to get information. */
object Calc {
  /** Average of a seq of numbers */
  private[this] def avg(seq: Seq[Int]) =
    seq.foldLeft(0.0)(_ + _) / seq.size


  /** Average mana cost of the whole deck. */
  def avgManaCost(d: Deck): Double =
    avg(d.cards.map(_.cmc))


  /** Average cost of cards that fulfill a condition. */
  def avgManaCost(deck: Deck, pred: Card => Boolean): Double =
    avg(deck.cards.filter(pred).map(_.cmc))


  /** Total number of cards. */
  def count(deck: Deck): Int = deck.cards.size


  /** Count the number of cards in a deck that match a certain predicate. */
  def count(deck: Deck, pred: Card => Boolean): Int =
    deck.cards.count(pred)


  /** Show a mana curve for cards that match a criterion. By default gives manacurve of all nonland
    * cards. The curve is a sequence of (manacost, amount), starting at mana cost zero up to the
    * greatest cost in the deck. All non-appearing costs within this range are included as zero.
    */
  def manaCurve(d: Deck, criterion: Card => Boolean = !_.is(Land)): Seq[(Int, Int)] = {
    // Compare tuples only by the first element.
    def lt(a: Tuple2[Int, Int], b: Tuple2[Int, Int]) = a._1 < b._1

    val map = Seqs.encode(d.cards.filter(criterion) map (_.cmc)).withDefault { x => 0 }

    map.toSeq.sortWith(lt)
  }


  /** Count Mana Symbols in cards in a deck that match a criterion (By default
    * all cards). A colored symbol counts once towards it's color. A generic mana
    * symbol counts for as much as it represents. A hybrid mana symbol counts
    * once towards each thing it represents.
    */
  def manaSymbols(d: Deck, criterion: Card => Boolean = _ => true): Map[String, Double] = {
    def mana2Map(m: Map[String, Double], mana: Mana, weight: Double): Map[String, Double] = mana match {
      case GenericMana(cmc, _)   => m.updated("C", m("C") + weight*cmc)
      case _: ColoredMana        => m.updated(mana.toString, m(mana.toString) + weight)
      case HybridMana(opts)      => opts.foldLeft(m) { mana2Map(_, _, 0.5)}    // We count both sides of hybrid as half the amount
      case ColorlessMana(props)  => m.updated("C", m("C") + weight * 1)
    }

    val mapCost = Map[String, Double]().withDefaultValue(0.0)

    val symbols = d.cards.filter(criterion).flatMap(c => c.cost)

    symbols.foldLeft(mapCost)((map, symb) => mana2Map(map, symb, 1.0))
  }

  /** Full calculations
    *
    * @param d The deck
    * @return [[DeckObject]] with the corresponding values
    */
  def fullCalc(d: Deck): DeckObject =
    DeckObject(avgManaCost(d),
               avgCMCNonLands = avgManaCost(d, c => !c.types.contains(Land)))
}
