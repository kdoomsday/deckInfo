package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.{Card, Mana}
import ebarrientos.deckStats.load.utils.{LoadUtils, URLUtils}
import org.json4s._
import org.json4s.native.JsonMethods._
import zio.Task
import requests.Response
import scala.annotation.tailrec
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype
import org.slf4j.LoggerFactory
import ebarrientos.deckStats.stringParsing.MtgJsonParser.{cost, parseAll}
import zio.ZIO

/** Loader para cargar información de api.magicthegathering.io */
object MagicIOLoader extends CardLoader with LoadUtils with URLUtils {
  private val log = LoggerFactory.getLogger(getClass())

  private[this] val baseUrl = "https://api.magicthegathering.io/v1/cards"

  def card(name: String): Task[Option[Card]] = {
    def getStr(value: JValue): String = value match {
      case JString(s) => s
      case _          => ""
    }
    def getInt(value: JValue): Int    = value match {
      case JInt(num)                              => num.toInt
      case JString(txt) if (txt matches "[0-9]+") => txt.toInt
      case _                                      => 0
    }

    /** In an array of results, find the one that fully matches the expected name.
      * This is important because the API finds partial matches (e.g. when
      * searching for 'Wasteland' a result will come in for 'Wasteland Scorpion'
      */
    def findObject(arr: JArray): Option[JValue] = {
      @tailrec
      def fo(l: List[JValue]): Option[JValue] =
        l match {
          case jobject :: rest =>
            if (getStr(jobject \ "name") == name)
              Some(jobject)
            else
              fo(rest)

          case Nil =>
            None
        }

      fo(arr.arr)
    }

    // Construir la carta a partir del jobject correspondiente ya extraido de la lista
    def cardFromJobject(j: JValue): Card = {
      val manaCost: Seq[Mana] = parseAll(cost, getStr(j \\ "manaCost")).get
      val (supertypes, types, subTypes) = parseTypesJson(j)
      Card(
        manaCost,
        name,
        types,
        supertypes,
        subTypes,
        getStr(j \ "text"),
        getInt(j \ "power"),
        getInt(j \ "toughness")
      )
    }

    ZIO.attemptBlocking {
      log.info(s"Requesting card $name from MagicIOLoader")
      val cardJsonResponse: Response = requests.get(
        baseUrl,
        params = Map("name" -> name),
        readTimeout = 20000,
        connectTimeout = 20000
      )

      if (cardJsonResponse.statusCode == 200) {
        (parse(cardJsonResponse.text) \\ "cards") match {
          case arr @ JArray(_) =>
            findObject(arr).map(cardFromJobject)
          case _               => None
        }
      }
      else {
        throw new Exception(
          s"Error loading card: Status ${cardJsonResponse.statusCode}"
        )
      }
    }
  }

  /** From json get subtertypes, types and subtypes */
  def parseTypesJson(
      cardJson: JValue
  ): (Set[Supertype], Set[CardType], Set[String]) = {

    val types = for {
      JString(t) <- (cardJson \ "types")
    } yield CardType(t)

    val supertypes = for {
      JString(t) <- (cardJson \ "supertypes")
    } yield Supertype(t)

    val subtypes = for {
      JString(t) <- (cardJson \ "subtypes")
    } yield t

    log.debug(s"""Types = ${types.mkString(", ")}""")
    (supertypes.toSet, types.toSet, subtypes.toSet)
  }
}
