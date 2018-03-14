package ebarrientos.deckStats.load

import ebarrientos.deckStats.load.utils.URLUtils
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.basics.Card

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonAST.JValue
import org.json4s.JsonAST.JObject
import org.json4s.JsonAST.JString
import org.json4s.JsonAST.JArray

/**
 * CardLoader que lee la informaci&oacute;n de http://api.mtgapi.com/v1/card/name/{name}
 */
class ScryCardLoader(baseUrl: String = "http://scry.me.uk/api.php?name=")
  extends CardLoader with LoadUtils with URLUtils
{
  // TODO Manejar cuando no se consigue la carta
  def card(cardName: String): Option[Card] = {
    import ebarrientos.deckStats.stringParsing.ScryManaParser.{parseAll, cost}

    implicit val json: JValue = loadJson(cardName)

    val name = getValue("name")
    val pt = getValue("power_toughness")
    val typesLine = getValue("types")
    val text = getValue("card_text")
    val manaCost = getValue("mana_cost")

    val (supertypes, types, subtypes) = parseTypes(typesLine)
    val (power, toughness) = parsePT(pt)

    val c = Card(
        parseAll(cost, manaCost).get,
        name,
        types,
        supertypes,
        subtypes,
        text,
        power,
        toughness
    )
    Some(c)
  }


  /** Obtener un atributo de una carta. Si no lo consigue, devuelve la cadena vac&iacute;a */
  private[this] def getValue(att: String)(implicit json: JValue): String = {
    (json \ att) match {
      case JString(value) => value
      case _ => ""
    }
  }

  /** Cargar el json de busqueda por nombre de una carta */
  private[this] def loadJson(cardName: String): JValue = {
    val url = s"http://scry.me.uk/api.php?name=${sanitize(cardName)}"
    parse(readURL(url))
  }
}
