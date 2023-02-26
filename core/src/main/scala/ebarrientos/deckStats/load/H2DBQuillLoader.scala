package ebarrientos.deckStats.load

import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.config.CoreConfig
import org.slf4j.LoggerFactory
import zio.ZLayer
import zio.Task
import io.getquill._
import io.getquill.util.LoadConfig
import ebarrientos.deckStats.basics.Card
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariConfig
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.Supertype
import java.util.zip.ZipEntry
// import ebarrientos.deckStats.stringParsing.ManaParser.{cost, parseAll}
import ebarrientos.deckStats.stringParsing.MtgJsonParser
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.White
import ebarrientos.deckStats.basics.ColorlessMana
import ebarrientos.deckStats.basics.GenericMana
import ebarrientos.deckStats.run._
import javax.sql.DataSource
import zio.ZIO
import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.JavaConverters._
import java.nio.file.Path
import zio.Ref
import zio.UIO
import zio.Unsafe
import ebarrientos.deckStats.stringParsing.MtgJsonParser

/** Card loader using quill to handle database queries
  *
  * @param helper [[CardLoader]] to fetch info if not present in this one
  * @param ds [[DataSource]] to use
  * @param runner [[ZioRunner]] which will be used to ensure tables are initialized
  */
class H2DBQuillLoader(override val helper: CardLoader, ds: DataSource, runner: ZioRunner)
    extends CardLoader
    with StoringLoader
    with LoadUtils {

  // private val log = LoggerFactory.getLogger(getClass())
  private val parser = MtgJsonParser

  /** DataSource layer for the dao */
  private val dataSource: ZLayer[Any, Throwable, DataSource] =
    ZLayer.fromZIO(ZIO.succeed(ds))

  private val ctx = new H2ZioJdbcContext(LowerCase)
  import ctx._

  // Encoders/Decoders
  // Mana
  implicit private val manaDecoder =
    MappedEncoding[String, Seq[Mana]](costStr => parser.parseAll(parser.cost, costStr).get)

  implicit private val manaEncoder =
    MappedEncoding[Seq[Mana], String](cost => parser.stringify(cost))

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
      _.split(" ").collect {
        case st if st.nonEmpty => Supertype.apply(st)
      }.toSet
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
    log.debug("Asked to fetch [{}]", name)
    val q = quote(query[Card].filter(c => c.name == lift(name)))
    ctx
      .run(q)
      .tap(cs => ZIO.succeed(log.debug(s"Found $cs")))
      .map(_.headOption)
      .catchAll { ex =>
        ZIO.succeed(log.warn(s"Error loading from DB: ${ex.getMessage}")) *> ZIO.succeed(None)
      }
      .provide(dataSource)
  }

  protected def store(c: Card): Task[Unit] =
    ctx
      .run(quote(query[Card].insertValue(lift(c))))
      .tap(_ => ZIO.succeed(log.info("{} stored", c.name)))
      .map(_ => ())
      .provide(dataSource)

  /** Attempt to run table creation */
  private def runInitScripts(): Task[Unit] = {
    def runSingle(path: Path): ZIO[DataSource, Throwable, Unit] = {
      log.debug(s"Run script: $path")
      val source       = scala.io.Source.fromFile(path.toFile())
      val text         = source.getLines().mkString("\n")
      source.close()
      val createScript = quote(sql"#${text}".as[Update[Int]])
      ctx.run(createScript).unit
    }

    val scriptsPath    = Paths.get("dbInitScripts/")
    val scriptIterator = Files.list(scriptsPath).iterator().asScala.toList.sorted

    ZIO
      .collectAll(scriptIterator.map(runSingle))
      .provide(dataSource)
      .unit
  }

  /* Initialize the tables if necessary */
  runner.run(runInitScripts())
}
