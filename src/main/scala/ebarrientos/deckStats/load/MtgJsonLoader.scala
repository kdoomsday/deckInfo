package ebarrientos.deckStats.load
import ebarrientos.deckStats.basics.{Card, CardType, Mana, Supertype}
import ebarrientos.deckStats.load.MtgJsonLoader.Carta
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.stringParsing.ScryManaParser
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._

class MtgJsonLoader(source: => String) extends CardLoader with LoadUtils {
  val manaParseFunc: String => Seq[Mana] = ScryManaParser.parseAll(ScryManaParser.cost, _).get

  override def card(name: String): Option[Card] =
    byName(name).fold(_ => None, (result: Result[Carta]) => {
      result.fold(_ => None, c => Some(toCard(c)))
    })

  private[this] def byName(name: String): Either[ParsingFailure, Result[Carta]] =
    parse(source).map { json =>
      json.hcursor
          .downField(name)
          .as[Carta]
    }

  /** Convertir de [[Carta]] a [[Card]] */
  private[this] def toCard(c: Carta): Card = {
    val cost: Seq[Mana] = manaParseFunc(c.manaCost)
    val (supertypes, types, subtypes) = parseTypes(buildTypes(c.types, c.`type`, c.subtypes))
    val (power, toughness) = parsePT(c.power + "/" + c.toughness)
    Card(
      cost,
      c.name,
      types,
      supertypes,
      subtypes,
      c.text,
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
    subtypes: List[String],
    text: String,
    power: String,
    toughness: String,
    manaCost: String
  )

  implicit val cartaDecoder: Decoder[Carta]         = deriveDecoder
}


