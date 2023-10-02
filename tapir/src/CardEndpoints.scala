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

/** Endpoint definitions */
object CardEndpoints {
  import Schemas._
  import Codecs._

  def cardEndpoint: Endpoint[Unit, String, Unit, Card, Any] =
    endpoint
      .in("card" / path[String])
      .get
      .out(jsonBody[Card])

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
    postDeck.serverLogic { content => Helpers.logicLive.deck(xmlFromContent(content)) }

  val allEndpoints = List(cardServerEndpoint, deckServerEndpoint)

  /** Extract the xmlDeck from the request */
  private def xmlFromContent(content: String): String = {
    val lines = content.split(Array('\n', '\r'))
    val boundary = lines.headOption.getOrElse("--")
    lines
      .dropWhile(!_.trim.startsWith("<?xml"))
      .filterNot(line => line.trim.startsWith(boundary))
      .mkString
  }
}

object Schemas {
  implicit lazy val colorSchema: Schema[Color]               = Schema.derived
  implicit lazy val manaPropertySchema: Schema[ManaProperty] = Schema.derived
  implicit lazy val manaSchema: Schema[Mana]                 = Schema.derived
  implicit lazy val cardTypeSchema: Schema[CardType]         = Schema.derived
  implicit lazy val supertypeSchema: Schema[Supertype]       = Schema.derived
  implicit lazy val cardSchema: Schema[Card]                 = Schema.derived

  implicit lazy val deckObjectSchema: Schema[DeckObject]     = Schema.derived
}
