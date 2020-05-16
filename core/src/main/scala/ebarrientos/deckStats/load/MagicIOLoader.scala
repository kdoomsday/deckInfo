package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.{ Card, Mana }
import ebarrientos.deckStats.load.utils.{ LoadUtils, URLUtils }
import org.json4s._
import org.json4s.jackson.JsonMethods._
import zio.IO
import requests.Response

/** Loader para cargar información de api.magicthegathering.io */
object MagicIOLoader extends CardLoader with LoadUtils with URLUtils {

  private[this] val baseUrl = "https://api.magicthegathering.io/v1/cards"

  def card(name: String): IO[Throwable, Option[Card]] = {
    def getStr(value: JValue): String = value match {
      case JString(s) => s
      case _          => ""
    }
    def getInt(value: JValue): Int = value match {
      case JInt(num) => num.toInt
      case JString(txt) if (txt matches "[0-9]+") => txt.toInt
      case _         => 0
    }

    // Construir la carta a partir del jobject correspondiente ya extraido de la lista
    def cardFromJobject(j: JValue): Card = {
      import ebarrientos.deckStats.stringParsing.MagicApiManaParser.{parseAll, cost}

      val name = getStr(j \\ "name")
      val manaCost: Seq[Mana] = parseAll(cost, getStr(j \\ "manaCost")).get
      val (supertypes, types, subTypes) = parseTypes(getStr(j \\ "type"))
      Card(
        manaCost,
        name,
        types,
        supertypes,
        subTypes,
        getStr(j \\ "text"),
        getInt(j \\ "power"),
        getInt(j \\ "toughness")
      )
    }

    IO.effect {
      val cardJsonResponse: Response = requests.get(baseUrl, params=Map("name" -> name), readTimeout=20000, connectTimeout=20000)

      if (cardJsonResponse.statusCode == 200) {
        (parse(cardJsonResponse.text) \\ "cards") match {
          case JArray(jobject :: _) => Some(cardFromJobject(jobject))
          case _                    => None
        }
      }
      else {
        throw new Exception(s"Error loading card: Status ${cardJsonResponse.statusCode}")
      }
    }
  }
}
