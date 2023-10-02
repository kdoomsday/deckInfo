package ebarrientos.deckInfo

import zio.json.DeriveJsonEncoder
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.Color
import zio.json.JsonEncoder
import ebarrientos.deckStats.basics.ManaProperty
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype
import zio.json.DeriveJsonDecoder
import zio.json.JsonDecoder
import ebarrientos.deckStats.queries.DeckObject
import ebarrientos.deckStats.queries.CountObject
import ebarrientos.deckStats.queries.CurvePoint

object Codecs {
  implicit val colorEncoder: JsonEncoder[Color]               = DeriveJsonEncoder.gen
  implicit val manaPropertyEncoder: JsonEncoder[ManaProperty] = DeriveJsonEncoder.gen
  implicit val manaEncoder: JsonEncoder[Mana]                 = DeriveJsonEncoder.gen
  implicit val cardTypeEncoder: JsonEncoder[CardType]         = DeriveJsonEncoder.gen
  implicit val supertypeEncoder: JsonEncoder[Supertype]       = DeriveJsonEncoder.gen
  implicit val cardEncoder: JsonEncoder[Card]                 = DeriveJsonEncoder.gen
  implicit val countObjectEncoder: JsonEncoder[CountObject]   = DeriveJsonEncoder.gen
  implicit val curvePointEncoder: JsonEncoder[CurvePoint]     = DeriveJsonEncoder.gen
  implicit val deckObjectEncoder: JsonEncoder[DeckObject]     = DeriveJsonEncoder.gen

  implicit val colorDecoder: JsonDecoder[Color]               = DeriveJsonDecoder.gen
  implicit val manaPropertyDecoder: JsonDecoder[ManaProperty] = DeriveJsonDecoder.gen
  implicit val manaDecoder: JsonDecoder[Mana]                 = DeriveJsonDecoder.gen
  implicit val cardTypeDecoder: JsonDecoder[CardType]         = DeriveJsonDecoder.gen
  implicit val supertypeDecoder: JsonDecoder[Supertype]       = DeriveJsonDecoder.gen
  implicit val cardDecoder: JsonDecoder[Card]                 = DeriveJsonDecoder.gen
  implicit val countObjectDecoder: JsonDecoder[CountObject]   = DeriveJsonDecoder.gen
  implicit val curvePointDecoder: JsonDecoder[CurvePoint]     = DeriveJsonDecoder.gen
  implicit val deckObjectDecoder: JsonDecoder[DeckObject]     = DeriveJsonDecoder.gen
}
