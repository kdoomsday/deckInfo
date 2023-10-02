package ebarrientos.deckStats.stringParsing

import scala.util.parsing.combinator.JavaTokenParsers
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.GenericMana
import ebarrientos.deckStats.basics.XMana
import ebarrientos.deckStats.basics.HybridMana

object ScryManaParser extends JavaTokenParsers with ParserHelpers {
  def cost: Parser[Seq[Mana]] = rep(mana)
  def mana: Parser[Mana] = "{" ~> (phyrexian | hybrid | color | colorless | xMana) <~ "}"
  def color: Parser[Mana] = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
  def colorless: Parser[Mana] = wholeNumber ^^ (x => GenericMana(x.toInt))
  def xMana: Parser[Mana] = "X" ^^ (_ => XMana())
  def phyrexian: Parser[Mana] = "Phyrexian " ~> colorWord ^^ (x => Mana.asPhyrexian(x))
  def hybrid: Parser[Mana] = rep1sep[Mana](hybText, "or") ^^ (x => HybridMana(x.toSet))

  def hybText: Parser[Mana] = colorWord | longFormNumber
  def colorWord: Parser[Mana] =
    ("White" | "Blue" | "Black" | "Red" | "Green") ^^ (x => ColoredMana(longStr2Color(x)))

  def longFormNumber: Parser[Mana] = "[A-Z][a-z]+".r ^^ (x => GenericMana(letters2Number(x)))
}
