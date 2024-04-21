package ebarrientos.deckStats.basics

/** Possible card types */
enum CardType:
  case Creature, Artifact, Land, Instant, Sorcery, Tribal, Enchantment, Planeswalker, Battle

object CardType {
  def isType(s: String): Boolean = CardType.values.exists(_.toString() == s)

  def apply(typeName: String) =
    CardType.values.find(_.toString() == typeName)
      .getOrElse(throw new Exception("Unknown card type"))
}


/** Possible card supertypes */
enum Supertype:
  case Legendary, Basic, Host, Snow

object Supertype:
  def apply(s: String): Supertype =
    Supertype.values.find(_.toString == s)
      .getOrElse(throw new Exception(s"Unknown supertype: $s"))

  def unapply(st: Supertype) = Some(st.toString)

  def isSupertype(s: String): Boolean = Supertype.values.exists(_.toString() == s)
