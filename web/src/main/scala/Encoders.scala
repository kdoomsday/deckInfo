package com.ebarrientos.deckInfo.web

import io.circe._
import io.circe.generic.semiauto._
import ebarrientos.deckStats.basics._

object Encoders {
  implicit val manaPropertyEncoder: Encoder[ManaProperty] = new Encoder[ManaProperty] {
    def apply(a: ManaProperty): Json = a match {
      case Phyrexian => Json.fromString("{P}")
      case Snow => Json.fromString("{S}")
    }
  }

  implicit val colorEncoder: Encoder[Color] = Encoder.encodeString.contramap(_.toString())

  implicit val manaEncoder: Encoder[Mana] = Encoder.encodeString.contramap(_.toString)

  implicit val cardTypeEncoder: Encoder[CardType] = Encoder.encodeString.contramap(_.toString())
  implicit val superTypeEncoder: Encoder[Supertype] = Encoder.encodeString.contramap(_.toString())

  implicit val cardEncoder: Encoder[Card] = deriveEncoder
}
