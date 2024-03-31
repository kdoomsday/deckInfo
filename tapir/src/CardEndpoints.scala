package ebarrientos.deckInfo

import sttp.tapir.ztapir._
import ebarrientos.deckStats.basics.Card
import sttp.tapir.Schema
import sttp.tapir.generic.auto._
import sttp.tapir.json.zio._
import ebarrientos.deckStats.basics.Color
import ebarrientos.deckStats.basics.ManaProperty
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype
import sttp.tapir.Endpoint
import ebarrientos.deckStats.queries.DeckObject
import ebarrientos.deckStats.basics.{Black, Blue, Green, Red, White}

/** Endpoint definitions */
object CardEndpoints {
  import Schemas._
  import Codecs._

  def cardEndpoint: Endpoint[Unit, String, Unit, Card, Any] =
    val res =
      endpoint
        .get
        .in("card" / path[String])
        .out(jsonBody[Card])
        .description("Endpoint to query info about a card by its name")
    res

  def postDeck =
    endpoint
      .post
      .in("deck")
      .in(stringBody)
      .out(jsonBody[DeckObject])

}

/** Server definitions */
object CardServerEndpoints {
  import CardEndpoints._

  def cardServerEndpoint: ZServerEndpoint[Any, Any] =
    cardEndpoint.serverLogicOption(name => Helpers.logicLive.card(name))

  def deckServerEndpoint: ZServerEndpoint[Any, Any] =
    postDeck.serverLogic(content => Helpers.logicLive.deck(xmlFromContent(content)))

  val allEndpoints = List(cardServerEndpoint, deckServerEndpoint)

  /** Extract the xmlDeck from the request */
  private def xmlFromContent(content: String): String = {
    val lines    = content.split(Array('\n', '\r'))
    val boundary = lines.headOption.getOrElse("--")
    lines
      .dropWhile(!_.trim.startsWith("<?xml"))
      .filterNot(line => line.trim.startsWith(boundary))
      .mkString
  }
}

object Schemas {
  implicit def wSchema: Schema[White.type]              = Schema.string
  implicit def uSchema: Schema[Blue.type]               = Schema.string
  implicit def bSchema: Schema[Black.type]              = Schema.string
  implicit def rSchema: Schema[Red.type]                = Schema.string
  implicit def gSchema: Schema[Green.type]              = Schema.string
  implicit def colorSchema: Schema[Color]               = Schema.derived
  implicit def manaPropertySchema: Schema[ManaProperty] = Schema.derived
  implicit def manaSchema: Schema[Mana]                 = Schema.derived
  implicit def cardTypeSchema: Schema[CardType]         = Schema.derived
  implicit def supertypeSchema: Schema[Supertype]       = Schema.derived
  implicit def cardSchema: Schema[Card]                 = Schema.derived

  implicit lazy val deckObjectSchema: Schema[DeckObject] = Schema.derived
}
