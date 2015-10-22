package ebarrientos.deckStats.stringParsing

import scala.util.parsing.combinator.JavaTokenParsers
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.White
import ebarrientos.deckStats.basics.Blue
import ebarrientos.deckStats.basics.Green
import ebarrientos.deckStats.basics.Black
import ebarrientos.deckStats.basics.Red
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.Color
import ebarrientos.deckStats.basics.ColorlessMana
import ebarrientos.deckStats.basics.HybridMana
import ebarrientos.deckStats.basics.XMana

object ManaParser extends JavaTokenParsers with ParserHelpers {
	def cost: Parser[Seq[Mana]] = rep(mana)
	def mana: Parser[Mana] = color | colorless | xMana | hybrid | phyrexian
	def color: Parser[Mana] = ("W" | "U" | "B" | "R" | "G") ^^ (x => str2Mana(x))
	def colorless: Parser[Mana] = wholeNumber ^^ (x => ColorlessMana(x.toInt))
	def xMana: Parser[Mana] = "X" ^^ (x => XMana())
	def hybrid: Parser[Mana] = "("~>rep1sep[Mana](mana, "/")<~")" ^^ (x => HybridMana(x.toSet))
	def phyrexian: Parser[Mana] = "("~>mana<~"/P)"
}