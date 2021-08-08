package ebarrientos.deckStats.load

import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.config.CoreConfig
import org.slf4j.LoggerFactory
import zio.ZLayer
import zio.ZManaged
import zio.Task
import io.getquill._
import io.getquill.util.LoadConfig
import ebarrientos.deckStats.basics.Card
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype
import io.getquill
import java.util.zip.ZipEntry
import ebarrientos.deckStats.stringParsing.MtgJsonParser.{cost, parseAll}
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.White
import ebarrientos.deckStats.basics.ColorlessMana
import ebarrientos.deckStats.basics.GenericMana

/** Card loader using quill to handle database queries
  *
  * @param helper [[CardLoader]] to fetch info if not present in this one
  * @param config
  */
class H2DBQuillLoader(val helper: CardLoader, config: CoreConfig)(implicit
    zenv: zio.ZEnv
) extends CardLoader
    with StoringLoader
    with LoadUtils {

  private val log = LoggerFactory.getLogger(getClass())

  private val zioConn =
    ZLayer.fromManaged(for {
      ds   <- ZManaged.fromAutoCloseable(
                Task(JdbcContextConfig(LoadConfig("database")).dataSource)
              )
      conn <- ZManaged.fromAutoCloseable(Task(ds.getConnection))
    } yield conn)

  private val ctx = new PostgresZioJdbcContext(LowerCase)
  import ctx._

  // Encoders/Decoders
  // Mana
  implicit private val manaDecoder =
    MappedEncoding[String, Seq[Mana]](costStr => parseAll(cost, costStr).get)

  implicit private val manaEncoder =
    MappedEncoding[Seq[Mana], String](_.mkString)

  // Card types
  implicit private val cardTypeDecoder =
    MappedEncoding[String, Set[CardType]](
      _.split(" ").map(CardType.apply).toSet
    )

  implicit private val cardTypeEncoder =
    MappedEncoding[Set[CardType], String](_.mkString(" "))

  // Supertypes
  implicit private val superTypeDecoder =
    MappedEncoding[String, Set[Supertype]](
      _.split(" ").map(Supertype.apply).toSet
    )

  implicit private val superTypeEncoder =
    MappedEncoding[Set[Supertype], String](_.mkString(" "))

  // Subtypes
  implicit private val subTypeDecoder =
    MappedEncoding[String, Set[String]](_.split(" ").toSet)

  implicit private val subTypeEncoder =
    MappedEncoding[Set[String], String](_.mkString(" "))
  // End Encoders/Decoders

  protected def retrieve(name: String): Task[Option[Card]] = {
    val q = quote { query[Card].filter(c => c.name == lift(name)) }
    ctx
      .run(q)
      .map(_.headOption)
      .provideCustomLayer(zioConn)
      .provide(zenv)
  }

  protected def store(c: Card): Task[Unit] =
    ctx
      .run(quote { query[Card].insert(lift(c)) })
      .provideCustomLayer(zioConn)
      .provide(zenv)
      .map(_ => ())
}
