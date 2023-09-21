package ebarrientos.deckStats.load.utils

import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype

/** Utility methods for loading cards */
trait LoadUtils {

  /**
   * Get a list of supertypes, types and subtypes, given a standard typeline.
   */
  def parseTypes(typeLine: String):
    (Set[Supertype], Set[CardType], Set[String]) =
  {
    val ind = typeLine.indexOf("-")
    val (types, subtypes) = if (ind > -1) (typeLine take ind, typeLine drop (ind+1))
                            else (typeLine, "")

    var restypes = IndexedSeq[CardType]()
    var ressuper = IndexedSeq[Supertype]()

    types.split(" ").foreach(x => {
      if (CardType.isType(x)) restypes = restypes :+ CardType(x)
      else if (Supertype.isSupertype(x)) ressuper = ressuper :+ Supertype(x)
    })

    // Be careful not to create a Set with an empty string instead of empty set
    val subtypeset = if (subtypes == "") Set.empty[String]
                     else subtypes.trim.split(" ").toSet

    (ressuper.toSet, restypes.toSet, subtypeset)
  }


  // Parse a power / toughness string into values. Expects a valid P/T or an empty string
  def parsePT(pt: String): (Int, Int) = {
    if (pt == "") (0, 0)
    else {
      val vals = pt.split("/")
      (strToInt(vals.head.trim), strToInt(vals.tail.head.trim))
    }
  }

  // Convert power or toughness to Int, taking into consideration things like '*'
  private[this] def strToInt(s: String) = {
    if (s.contains("*") || s.contains("X")) 0
    else if (s.contains("+")) s.takeWhile(_ != '+').toInt
    else s.toInt
  }
}