package ebarrientos.deckStats.basics

sealed trait ManaProperty
case object Phyrexian extends ManaProperty
case object Snow extends ManaProperty


/** Common behavior for mana */
sealed trait Mana {
  def is(c: Color): Boolean
  def isColorless: Boolean

  def properties: Set[ManaProperty]
  def hasProperty(p: ManaProperty): Boolean = properties(p)

  def cmc: Int
}

object Mana {
  def asPhyrexian(mana: Mana): Mana = mana match {
    case m @ GenericMana(_, properties) => m.copy(properties = properties + Phyrexian)
    case m @ ColoredMana(_, properties) => m.copy(properties = properties + Phyrexian)
    case m @ HybridMana(_) => m // No tiene sentido phyrexianizar hibrido
    case m @ ColorlessMana(properties) => m.copy(properties + Phyrexian)
  }
}


/** Generic Mana, meaning mana with no requirements */
case class GenericMana( override val cmc: Int,
    					  				override val properties: Set[ManaProperty] = Set() )
  extends Mana
{
  override def is(c: Color) = false
  override val isColorless = true
  override def toString: String = "o" + cmc.toString
}

case class ColorlessMana(override val properties: Set[ManaProperty]) extends Mana {
  override def is(c: Color): Boolean = false
  override def isColorless: Boolean = true
  override def cmc: Int = 1
  override def hasProperty(p: ManaProperty): Boolean = false
  override def toString: String = "C"
}


/** Represents X costs. */
class XMana(properties: Set[ManaProperty] = Set())
  extends GenericMana(0, properties)
{
  override def toString = "X"
}
object XMana {
  def apply(properties: Set[ManaProperty] = Set()) = new XMana(properties)
  def unapply(properties: Set[ManaProperty]) = Some(properties)
}


/** Each instance is one colored mana. */
case class ColoredMana(color: Color, override val properties: Set[ManaProperty] = Set())
extends Mana
{
  override def is(c: Color): Boolean = color == c
  override val isColorless: Boolean = false
  override val cmc = 1
  override def toString: String = color match {
    case White => "W"
    case Blue  => "U"
    case Black => "B"
    case Red   => "R"
    case Green => "G"
  }
}



/**
 * Hybrid Mana implementation. Contains a set of options that describe it.
 */
case class HybridMana(options: Set[Mana]) extends Mana {
  def this(opts: List[Mana]) = this(opts.toSet)

  // It *is* of a color if at least one of the options is
  override def is(c: Color): Boolean =
    options.exists(mana => mana.is(c))

  // It's colorless if all options are colorless. Can't happen yet, but might with properties
  override def isColorless: Boolean = options.forall(_.isColorless)


  /** All properties this hybrid mana has. It's the union of all properties in all mana symbols
    * that conform it.
    */
  override lazy val properties: Set[ManaProperty] =
    options.foldLeft(Set[ManaProperty]())((rs, mana) => rs ++ mana.properties)

  override def hasProperty(p: ManaProperty): Boolean =
    options.exists(mana => mana.hasProperty(p))


  /** Converted mana cost. Max of cost of all options */
  override def cmc: Int = options.map(_.cmc).max

  override def toString: String = "H(" + options.mkString("/") + ")"
}
