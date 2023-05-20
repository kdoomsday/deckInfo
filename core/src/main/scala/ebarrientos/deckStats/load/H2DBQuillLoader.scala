package ebarrientos.deckStats.load

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.CardType
import ebarrientos.deckStats.basics.ColoredMana
import ebarrientos.deckStats.basics.ColorlessMana
import ebarrientos.deckStats.basics.GenericMana
import ebarrientos.deckStats.basics.Mana
import ebarrientos.deckStats.basics.Supertype
import ebarrientos.deckStats.basics.White
import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.load.utils.LoadUtils
import ebarrientos.deckStats.run._
import ebarrientos.deckStats.stringParsing.MtgJsonParser
import io.getquill._
import io.getquill.util.LoadConfig
import org.slf4j.LoggerFactory
import zio.Ref
import zio.Task
import zio.UIO
import zio.Unsafe
import zio.ZIO
import zio.ZLayer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import javax.sql.DataSource
import scala.collection.JavaConverters._

/** Card loader using quill to handle database queries
  *
  * @param helper
  *   [[CardLoader]] to fetch info if not present in this one
  * @param ds
  *   [[DataSource]] to use
  * @param runner
  *   [[ZioRunner]] which will be used to ensure tables are initialized
  */
class H2DBQuillLoader(val helper: CardLoader, ds: DataSource, initScriptsPath: String, runner: ZioRunner)
    extends CardLoader
    with LoadUtils {

  private val log    = LoggerFactory.getLogger(getClass())
  private val parser = MtgJsonParser

  /** DataSource layer for the dao */
  private val dataSource: ZLayer[Any, Throwable, DataSource] =
    ZLayer.fromZIO(ZIO.succeed(ds))

  private val ctx = new H2ZioJdbcContext(LowerCase)
  import ctx._

  // Encoders/Decoders
  // Mana
  implicit private val manaDecoder: MappedEncoding[String, Seq[Mana]] =
    MappedEncoding[String, Seq[Mana]](costStr => parser.parseAll(parser.cost, costStr).get)

  implicit private val manaEncoder: MappedEncoding[Seq[Mana], String] =
    MappedEncoding[Seq[Mana], String](cost => parser.stringify(cost))

  // Card types
  implicit private val cardTypeDecoder: MappedEncoding[String, Set[CardType]] =
    MappedEncoding[String, Set[CardType]](
      _.split(" ").map(CardType.apply).toSet
    )

  implicit private val cardTypeEncoder: MappedEncoding[Set[CardType], String] =
    MappedEncoding[Set[CardType], String](_.mkString(" "))

  // Supertypes
  implicit private val superTypeDecoder: MappedEncoding[String, Set[Supertype]] =
    MappedEncoding[String, Set[Supertype]](
      _.split(" ").collect {
        case st if st.nonEmpty => Supertype.apply(st)
      }.toSet
    )

  implicit private val superTypeEncoder: MappedEncoding[Set[Supertype], String] =
    MappedEncoding[Set[Supertype], String](_.mkString(" "))

  // Subtypes
  implicit private val subTypeDecoder: MappedEncoding[String, Set[String]] =
    MappedEncoding[String, Set[String]](_.split(" ").toSet)

  implicit private val subTypeEncoder: MappedEncoding[Set[String], String] =
    MappedEncoding[Set[String], String](_.mkString(" "))
  // End Encoders/Decoders

  override def card(name: String): Task[Option[Card]] =
    retrieve(name)
      .flatMap { oc =>
        if (oc.isDefined) ZIO.succeed(oc)
        else {
          helper
            .card(name)
            .flatMap(oc => maybeStore(oc) *> ZIO.succeed(oc))
        }
      }

  /** Get card from DB, do not involve helper */
  private def retrieve(name: String): Task[Option[Card]] = {
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

  /** Store the card, if it is present */
  private def maybeStore(oc: Option[Card]): Task[Unit] =
    oc.map(c => store(c)).getOrElse(ZIO.unit)

  private def store(c: Card): Task[Unit] =
    ctx
      .run(quote(query[Card].insertValue(lift(c))))
      .tap(_ => ZIO.succeed(log.info("{} stored", c.name)))
      .map(_ => ())
      .provide(dataSource)

  /**
   * Store multiple cards at once
   *
   * @param cards Cards to store
   * @return Number of cards stores. Assumes all were inserted
   */
  private def storeMulti(cards: Seq[Card]): Task[Long] =
    ctx
      .run(liftQuery(cards).foreach(c => quote(query[Card].insertValue(c))))
      .map(_ => cards.size.toLong)
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

    ZIO
      .attempt {
        val initPath = Paths.get(initScriptsPath)
        log.debug(s"Getting scripts from root path $initPath")
        val scriptIterator = Files.list(initPath).iterator().asScala.toList.sorted
        scriptIterator
      }
      .flatMap(si => ZIO.collectAll(si.map(runSingle)))
      .provide(dataSource)
      .unit
  }

  /** Queries for all cards at once, without resorting to the helper
    *
    * @param names
    *   All the looked for cards
    * @return
    *   All of the cards that were found. This might be less than the requested cards
    */
  private def queryMultiple(names: Seq[String]): Task[Seq[Card]] = {
    val q = quote {
      query[Card]
        .filter(c => liftQuery(names).contains(c.name))
    }

    ctx
      .run(q)
      .tap(res => ZIO.succeed(log.debug("Query found {} cards", res.size)))
      .provide(dataSource)
  }

  /** Implement {{cards(names)}} so we can leverage querying multiple cards at once */
  override def cards(names: Seq[String]): ZIO[Any, Throwable, Seq[Card]] =
    for {
      storedCards  <- queryMultiple(names)
      missingNames <- ZIO.succeed(names.diff(storedCards.map(_.name)))
      helperCards  <- helper.cards(missingNames)
      _            <- storeMulti(helperCards)
    } yield storedCards ++ helperCards

  private def logTotalCardCount(): Task[Unit] =
    ctx
      .run(quote(query[Card].size))
      .map(count => log.debug(s"Found $count cards."))
      .provide(dataSource)

  log.debug("Attempting to run init scripts")
  /* Initialize the tables if necessary */
  runner.run(
    (runInitScripts() *> logTotalCardCount()).catchAll { ex =>
      log.error("Error initializing database", ex)
      ZIO.fail(ex)
    }
  )
  log.debug(s"Finished initialization of ${getClass()}")
}
