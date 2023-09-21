package ebarrientos.deckStats.stringParsing

import scala.util.parsing.combinator.JavaTokenParsers
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.XMana
import ebarrientos.deckStats.basics.GenericMana
import ebarrientos.deckStats.basics.HybridMana
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.GenericMana

/** Mana parser for json results from mtg-api.
  * It doesn't support phyrexian mana yet.
  */
object MagicApiManaParser extends JavaTokenParsers with ParserHelpers {
  def cost: Parser[Seq[Mana]] = rep(mana)
  def mana: Parser[Mana] = "{" ~> (hybrid | color | colorless | xMana ) <~ "}"
  def color: Parser[Mana] = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
  def colorless: Parser[Mana] = wholeNumber ^^ (x => GenericMana(x.toInt))
  def xMana: Parser[Mana] = "X" ^^ (x => XMana())
  def hybrid: Parser[Mana] = rep1sep[Mana](hybText, "or") ^^ (x => HybridMana(x.toSet))

  def hybText: Parser[Mana] = colorWord | longFormNumber
  def colorWord: Parser[Mana] =
    ("White" | "Blue" | "Black" | "Red" | "Green") ^^ (x => ColoredMana(longStr2Color(x)))

  def longFormNumber: Parser[Mana] = "[A-Z][a-z]+".r ^^ (x => GenericMana(letters2Number(x)))
}