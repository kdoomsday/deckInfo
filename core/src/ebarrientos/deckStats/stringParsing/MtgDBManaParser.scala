package ebarrientos.deckStats.stringParsing

import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.XMana
import ebarrientos.deckStats.basics.HybridMana
import ebarrientos.deckStats.basics.GenericMana
import scala.util.parsing.combinator.JavaTokenParsers

/** Mana parser for json results from mtgdb (http://api.mtgdb.info/). */
object MtgDBManaParser extends JavaTokenParsers with ParserHelpers {
  def cost: Parser[Seq[Mana]] = rep(mana)
  def mana: Parser[Mana]      = color | colorless | xMana | hybrid | phyrexian
  def color: Parser[Mana]     = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
  def colorless: Parser[Mana] = wholeNumber ^^ (x => GenericMana(x.toInt))
  def xMana: Parser[Mana]     = "X" ^^ (_ => XMana())
  def hybrid: Parser[Mana]    = "{" ~> rep1sep[Mana](mana, "/") <~ "}" ^^ (x => HybridMana(x.toSet))

  def phyrexian: Parser[Mana] =
    "{" ~> mana <~ "P}" // We don't distinguish phyrexian mana from regular
}
