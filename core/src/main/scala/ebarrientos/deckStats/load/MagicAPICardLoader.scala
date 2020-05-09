package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.load.utils.URLUtils
import zio.IO

/** Cardloader that gets card info from http://stegriff.co.uk/ */
class MagicAPICardLoader extends CardLoader with LoadUtils with URLUtils {

  override def card(name: String): IO[Throwable, Option[Card]] = {
    println(s"Loading: $name")
    cardMap(name).map(om => om.map(m => cardFromMap(name, m)))
  }



  /** Load a map with the json bits of the card from the web api. The map has the empty string set
    * as the default value.
    * If there was an error loading, or the card doesn't exist, returns None. Otherwise, returns a
    * Some with the map that contains all relevant values.
    */
  private[this] def cardMap(name: String): IO[Throwable, Option[Map[String, String]]] = {
    import util.parsing.json.JSON

    for {
      cardStr <- ioReadUrl(s"http://stegriff.co.uk/host/magic/?name=$name")
    } yield parsedJson2Map(JSON.parseFull(cardStr))
  }

  /** Convert from maybe a json thing to maybe a map of what was in it */
  private[this] def parsedJson2Map(parsed: Option[Any]): Option[Map[String, String]] =
    parsed flatMap (m => {
                      val castMap = m.asInstanceOf[Map[String, String]].withDefaultValue("")
                      if (castMap.contains("error")) None
                      else Some(castMap)
                    })


  /** Obtener un [[Card]] del mapa. El mapa se asume que SI tiene la carta */
  private[this] def cardFromMap(name: String, map: Map[String, String]): Card = {
    import ebarrientos.deckStats.stringParsing.MagicApiManaParser.{parseAll, cost}

    val (supertypes, types, subtypes) = parseTypes(map("types"))
    val (power, toughness) = parsePT( map("power_toughness") )

    Card (
        parseAll(cost, map("mana_cost")).get,
        name,
        types,
        supertypes,
        subtypes,
        map("card_text"),
        power,
        toughness
    )
  }
}
