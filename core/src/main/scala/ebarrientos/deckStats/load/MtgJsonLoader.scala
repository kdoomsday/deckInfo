package ebarrientos.deckStats.load
import ebarrientos.deckStats.basics.{Card, CardType, Mana, Supertype}
import ebarrientos.deckStats.load.MtgJsonLoader.Carta
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.stringParsing.{MagicApiManaParser, MtgJsonParser, ScryManaParser}
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import zio._

class MtgJsonLoader(source: => String) extends CardLoader with LoadUtils {
  val manaParseFunc: String => Seq[Mana] = MtgJsonParser.parseAll(MtgJsonParser.cost, _).get

  override def card(name: String): IO[Exception, Option[Card]] =
    byName(name).map(_.toOption.map(toCard _))

  private[this] def byName(name: String): IO[ParsingFailure, Result[Carta]] =
    ZIO.fromEither {
      parse(source).map { json =>
        json.hcursor
          .downField(name)
          .as[Carta]
      }
    }

  /** Convertir de [[Carta]] a [[Card]] */
  private[this] def toCard(c: Carta): Card = {
    val cost: Seq[Mana] = c.manaCost.fold(Seq[Mana]())(manaParseFunc(_))
    val (supertypes, types, subtypes) = parseTypes(buildTypes(c.types, c.`type`, c.subtypes.getOrElse(Nil)))
    val (power, toughness) = parsePT(c.power.getOrElse(0) + "/" + c.toughness.getOrElse(0))
    Card(
      cost,
      c.name,
      types,
      supertypes,
      subtypes,
      c.text.getOrElse(""),
      power,
      toughness
    )
  }

  private[this] def buildTypes( supertypes: Seq[String],
                                `type`: String,
                                subtypes: Seq[String] ): String =
    s"""${supertypes.mkString(" ")} ${`type`} - ${subtypes.mkString(" ")}"""
}

object MtgJsonLoader {
  def apply(source: => String) = new MtgJsonLoader(source)

  case class Carta(
    name: String,
    cmc: Int,
    types: List[String],
    `type`: String,
    subtypes: Option[List[String]],
    text: Option[String],
    power: Option[String],
    toughness: Option[String],
    manaCost: Option[String]
  )

  implicit val cartaDecoder: Decoder[Carta]         = deriveDecoder
}


