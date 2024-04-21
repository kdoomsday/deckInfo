package ebarrientos.deckStats.stringParsing

import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.Color
import ebarrientos.deckStats.basics.Color.*

/** Common methods for parsers. */
trait ParserHelpers {

  /** Convert a String to Color */
  def str2Mana(s: String): ColoredMana = {
    def str2Color(cs: String): Color = cs match {
      case "W" => White
      case "U" => Blue
      case "B" => Black
      case "R" => Red
      case "G" => Green
      case _ => throw new Exception("Don't know what color this is")
    }

    ColoredMana(str2Color(s))
  }

  /** Long String representing a color to Color */
  def longStr2Color(s: String): Color = s match {
    case "White" => White
    case "Blue" => Blue
    case "Black" => Black
    case "Red" => Red
    case "Green" => Green
    case _ => throw new Exception("Don't know what color this is")
  }


  /** Convert a numeric representation in letters to Int */
  def letters2Number(s: String): Int = s match {
    case "Zero" => 0
    case "One"  => 1
    case "Two" => 2
    case "Three" => 3
    case "Four" => 4
    case "Five" => 5
    case "Siz" => 6
    case "Seven" => 7
    case "Eight" => 8
    case "Nine" => 9
    case "Ten" => 10
    case "Eleven" => 11
    case "Twelve" => 12
    case "Thirteen" => 13
    case "Fourteen" => 14
    case "Fifteen" => 15
    case "Sixteen" => 16
    case _ => throw new Exception("Unknown letter representation of a number: " + s)
    // I stopped at 16 because it's the highest number I know of in a card (barring B.F.M.)
  }
}
