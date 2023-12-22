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
import scala.concurrent.duration.FiniteDuration
import zio.Schedule
import zio.Duration

/** Loader para cargar información de api.magicthegathering.io */
class MagicIOLoader(val timeout: FiniteDuration, retryTime: FiniteDuration, requester: MagicIOLoader.RequestParams => Response)
    extends CardLoader
    with LoadUtils
    with URLUtils {

  /** Alternate constructor that uses MagicIOLoader.requestsCallCard by default as the requester */
  def this(timeout: FiniteDuration, retryTime: FiniteDuration) = this(timeout, retryTime, MagicIOLoader.requestsCallCard)

  private val log = LoggerFactory.getLogger(getClass())


  def card(name: String): Task[Option[Card]] =
    ZIO
      .attemptBlocking {
        log.info(s"Requesting card $name from MagicIOLoader")
        requester(MagicIOLoader.RequestParams(name, timeout))
      }
      .flatMap { cardJsonResponse =>
        if (cardJsonResponse.statusCode == 200) {
          cardFromJsonString(name, cardJsonResponse.text())
        }
        else {
          ZIO.fail(new Exception(s"Error loading card: Status ${cardJsonResponse.statusCode}"))
        }
      }
      .retry(Schedule.fibonacci(Duration.Finite(retryTime.toNanos)))
      .catchAll { case ex =>
        log.error(s"Error querying card $name", ex)
        ZIO.succeed(None)
      }

  /** From json get supertypes, types and subtypes */
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

  /** Given the card's json string, parse the card */
  def cardFromJsonString(name: String, cardJson: String): Task[Option[Card]] =
    ZIO.attempt {
      (parse(cardJson) \\ "cards") match {
        case arr @ JArray(_) =>
          findObject(name, arr).map(ojValue => cardFromJobject(name, ojValue))
        case _               => None
      }
    }

  /** In an array of results, find the one that fully matches the expected name. This is important
    * because the API finds partial matches (e.g. when searching for 'Wasteland' a result will come
    * in for 'Wasteland Scorpion'
    */
  private def findObject(name: String, arr: JArray): Option[JValue] = {
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
  private def cardFromJobject(name: String, j: JValue): Card = {
    val manaCost: Seq[Mana]           = parseAll(cost, getStr(j \\ "manaCost")).get
    val (supertypes, types, subTypes) = parseTypesJson(j)
    Card(
      manaCost,
      name,
      types,
      supertypes,
      subTypes,
      getStr(j \ "text"),
      getInt(j \ "power"),
      getInt(j \ "toughness"),
      Some(getInt(j \ "multiverseid"))
    )
  }

  private def getStr(value: JValue): String = value match {
    case JString(s) => s
    case _          => ""
  }

  private def getInt(value: JValue): Int = value match {
    case JInt(num)                              => num.toInt
    case JString(txt) if (txt matches "[0-9]+") => txt.toInt
    case _                                      => 0
  }
}

object MagicIOLoader {
  case class RequestParams(cardName: String, timeout: FiniteDuration)

  val baseUrl = "https://api.magicthegathering.io/v1/cards"

  /** Make the call to get a card
   *
   * @param name Name of the card
   * @return Response
   */
  private def requestsCallCard: RequestParams => Response =
    requestParams => requests.get(
      baseUrl,
      params = Map("name" -> requestParams.cardName),
      readTimeout = requestParams.timeout.toMillis.toInt,
      connectTimeout = requestParams.timeout.toMillis.toInt
    )
}
