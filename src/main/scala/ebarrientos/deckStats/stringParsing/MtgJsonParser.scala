package ebarrientos.deckStats.stringParsing

import ebarrientos.deckStats.basics._

import scala.util.parsing.combinator.JavaTokenParsers

/** Mana parser for MtgJson */
object MtgJsonParser extends JavaTokenParsers with ParserHelpers {
  def cost:      Parser[Seq[Mana]] = rep(manaDef)

  def manaDef:   Parser[Mana] = "{" ~> mana <~ "}"
  def mana:      Parser[Mana] = rep1sep[Mana](nonHybrid, "/") ^^ (xs => seq2Mana(xs))

  def basicMana: Parser[Mana] = color | colorless | xMana
  def nonHybrid: Parser[Mana] = phyrexian | basicMana
  def phyrexian: Parser[Mana] = basicMana <~ "/P" ^^ (m => Mana.asPhyrexian(m))
  def color:     Parser[Mana] = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
  def colorless: Parser[Mana] = wholeNumber ^^ (x => ColorlessMana(x.toInt))
  def xMana:     Parser[Mana] = "X" ^^ (_ => XMana())


  /** De secuencia de mana a mana. Si hay 1 elemento se devuelve el elemento.
    * Si no, se considera mana híbrido
    */
  private[this] def seq2Mana(ms: Seq[Mana]): Mana = if (ms.size == 1) ms.head
                                                    else HybridMana(ms.toSet)
}
