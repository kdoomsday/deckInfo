package ebarrientos.deckStats.stringParsing

import ebarrientos.deckStats.basics._

import scala.util.parsing.combinator.JavaTokenParsers

/** Mana parser for MtgJson */
object MtgJsonParser extends JavaTokenParsers with ParserHelpers {
  def cost:      Parser[Seq[Mana]] = rep(manaDef)

  def manaDef:   Parser[Mana] = "{" ~> mana <~ "}"
  def mana:      Parser[Mana] = rep1sep[Mana](nonHybrid, "/") ^^ (xs => seq2Mana(xs))

  def nonHybrid: Parser[Mana] = phyrexian | basicMana
  def basicMana: Parser[Mana] = color | generic | xMana | snow
  def phyrexian: Parser[Mana] = basicMana <~ "/P" ^^ (m => Mana.asPhyrexian(m))
  def color:     Parser[Mana] = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
  def generic:   Parser[Mana] = wholeNumber ^^ (x => GenericMana(x.toInt))
  def xMana:     Parser[Mana] = "X" ^^ (_ => XMana())
  def snow:      Parser[Mana] = "S" ^^ (_ => GenericMana(1, Set(SnowMana)))


  /** De secuencia de mana a mana. Si hay 1 elemento se devuelve el elemento.
    * Si no, se considera mana híbrido
    */
  private[this] def seq2Mana(ms: Seq[Mana]): Mana = if (ms.size == 1) ms.head
                                                    else HybridMana(ms.toSet)

  /** Convert one Mana into a string representation that this parser recognizes */
  def stringify(m: Mana): String = {
    def colorString(c: Color) = c match {
      case White => "W"
      case Blue  => "U"
      case Black => "B"
      case Red   => "R"
      case Green => "G"
    }

    def inner(mana:Mana): String = mana match {
      case ColoredMana(color, properties) => colorString(color) + (if (properties contains Phyrexian) "/P" else "")
      case _: XMana => "X"
      case GenericMana(cmc, _) => cmc.toString()
      case ColorlessMana(_) => "C"
      case HybridMana(options) => options.map(n => inner(n)).mkString("/")
    }

    s"{${inner(m)}}"
  }

  /** Convert a mana cost into a string via [[stringify(m:Mana)]] */
  def stringify(cost: Seq[Mana]): String =
    cost.map(m => stringify(m)).mkString
}
