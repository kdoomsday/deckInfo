package ebarrientos.deckStats.load

import ebarrientos.deckStats.load.utils.URLUtils
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.utils.LoadUtils

class MtgDBCardLoader extends CardLoader with LoadUtils with URLUtils {

  def card(name: String) = {
    val map = cardMap(name)

    if (map.isDefined) cardFromMap(name, map.get)
    else {
      throw new Exception("Couldn't load card: " + name)
      // TODO Localize exceptions
    }
  }


  private[this] def cardMap(name: String): Option[Map[String, Any]] = {
    println(s"Loading: $name")

    val unQuotesName = sanitizeName(name)
    val cardStr = readURL(s"http://api.mtgdb.info/cards/$unQuotesName")
    val parsed = scala.util.parsing.json.JSON.parseFull(cardStr)

    parsed map (lm => lm.asInstanceOf[List[Map[String,String]]].head)
  }

  private[this] def sanitizeName(name: String) =
    name.replace("'", "").replace(",", "")


  /** The card object from the map. */
  private[this] def cardFromMap(name: String, map: Map[String, Any]): Card = {

    import _root_.ebarrientos.deckStats.stringParsing.MtgDBManaParser.{parseAll, cost}

    val ts = map("type").asInstanceOf[String]
    val subts = map("subType").asInstanceOf[String]
    val typeline =
      if ((subts eq null) || subts == "null")
        ts
      else
        s"$ts - $subts"

    val (supertypes, types, subtypes) = parseTypes(typeline)

    Card (
        parseAll(cost, map("manaCost").asInstanceOf[String]).get,
        name,
        types,
        supertypes,
        subtypes,
        map("description").asInstanceOf[String],
        map("power").asInstanceOf[Double].toInt,
        map("toughness").asInstanceOf[Double].toInt
    )
  }
}