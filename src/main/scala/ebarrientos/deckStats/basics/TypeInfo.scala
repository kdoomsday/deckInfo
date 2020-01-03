package ebarrientos.deckStats.basics

/**
 * Possible card types.
 */
sealed abstract class CardType
object CardType {
  val types =
    IndexedSeq(Creature, Artifact, Land, Instant, Sorcery, Tribal, Enchantment, Planeswalker)

  // Convert a string into a type
  def apply(s: String): CardType = {
    types.foreach(x => if (s == x.toString()) return x)
    throw new Exception("Unknown card type")
  }

  def unapply(t: CardType) = Some(t.toString())

  def isType(s: String): Boolean = types.foldLeft(false)((p, n) => p || s == n.toString)
}

case object Creature     extends CardType
case object Artifact     extends CardType
case object Land         extends CardType
case object Instant      extends CardType
case object Sorcery      extends CardType
case object Tribal       extends CardType
case object Enchantment  extends CardType
case object Planeswalker extends CardType


/**
 * Possible card supertypes
 */
sealed abstract class Supertype
object Supertype {
  private[this] val supertypes = IndexedSeq(Legendary, Basic)

  def apply(s: String): Supertype = {
    supertypes.foreach(x => if (s == x.toString) return x)
    throw new Exception("Unknown supertype")
  }

  def unapply(st: Supertype) = Some(st.toString)

  def isSupertype(s: String): Boolean = supertypes.foldLeft(false)((p, n) => p || s == n.toString())
}

case object Legendary extends Supertype
case object Basic     extends Supertype
case object Host      extends Supertype
