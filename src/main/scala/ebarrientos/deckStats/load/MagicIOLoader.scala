package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.{ Card, Mana }
import ebarrientos.deckStats.load.utils.{ LoadUtils, URLUtils }
import org.json4s._
import org.json4s.jackson.JsonMethods._


/** Loader para cargar información de api.magicthegathering.io */
object MagicIOLoader extends CardLoader with LoadUtils with URLUtils {

  /** Url para la info de una carta especifica */
  private[this] def url(cardName: String): String =
    s"""https://api.magicthegathering.io/v1/cards?name="$cardName" """

  def card(name: String): Option[Card] = {
    def getStr(value: JValue): String = value match {
      case JString(s) => s
      case _          => ""
    }
    def getInt(value: JValue): Int = value match {
      case JInt(num) => num.toInt
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

    (parse(readURL(url(name))) \\ "cards") match {
      case JArray(jobject :: _) => Some(cardFromJobject(jobject))
      case _                    => None
    }
  }

}
