package ebarrientos.deckStats.load

import ebarrientos.deckStats.load.utils.URLUtils
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.utils.LoadUtils
import scala.util.parsing.json.JSON
import scalaz.zio.IO

@Deprecated
/** Ya no funciona la p&aacute;gina, as&iacute; que se recomienda otro loader */
class MtgDBCardLoader extends CardLoader with LoadUtils with URLUtils {

  def card(name: String): IO[Exception, Option[Card]] =
    cardMap(name).map(om => om.map(m => cardFromMap(name, m)))


  private[this] def cardMap(name: String): IO[Exception, Option[Map[String, Any]]] = {
    println(s"Loading: $name")

    val unQuotesName = sanitizeName(name)

    for {
      cardStr <- ioReadUrl(s"http://api.mtgdb.info/cards/$unQuotesName")
    } yield
        JSON.parseFull(cardStr)
            .map (lm => lm.asInstanceOf[List[Map[String,String]]].head)
  }

  private[this] def sanitizeName(name: String) =
    name.replace("'", "").replace(",", "")


  /** The card object from the map. */
  private[this] def cardFromMap(name: String, map: Map[String, Any]): Card = {

    import _root_.ebarrientos.deckStats.stringParsing.MtgDBManaParser.{parseAll, cost}

    val ts = map("type").asInstanceOf[String]
    val subts = map("subType").asInstanceOf[String]
    val typeline =
      if ((subts eq null) || subts == "null")
        ts
      else
        s"$ts - $subts"

    val (supertypes, types, subtypes) = parseTypes(typeline)

    Card (
        parseAll(cost, map("manaCost").asInstanceOf[String]).get,
        name,
        types,
        supertypes,
        subtypes,
        map("description").asInstanceOf[String],
        map("power").asInstanceOf[Double].toInt,
        map("toughness").asInstanceOf[Double].toInt
    )
  }
}
