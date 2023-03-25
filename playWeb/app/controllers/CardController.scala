package controllers

import com.fasterxml.jackson.core.PrettyPrinter
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import ebarrientos.deckStats.basics.{Card, Land}
import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.load._
import ebarrientos.deckStats.math.Calc
import ebarrientos.deckStats.queries.{DeckCalc, DeckObject}
import ebarrientos.deckStats.run
import io.circe.generic.auto._
import io.circe.syntax._
import org.h2.jdbcx.JdbcDataSource
import play.api.Logger
import play.api.http.Writeable
import play.api.libs.circe.Circe
import play.api.mvc.AnyContent
import play.api.mvc.{BaseController, ControllerComponents, MultipartFormData, Request, Result}
import play.mvc.Action
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import zio.{ZIO, Task, UIO}

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.sql.DataSource
import scala.concurrent.ExecutionContext

class CardController @Inject() (
    val controllerComponents: ControllerComponents,
    runner: run.ZioRunner
) extends BaseController
    with Circe {

  val log = Logger(getClass())

  private val loader: CardLoader = runner.run(CardController.loader(runner))

  def card(name: String) = Action { implicit request: Request[AnyContent] =>
    val res: ZIO[Any, Serializable, Result] = for {
      c   <- loader.card(name)
      card = c.getOrElse(CardController.nullCard)
    } yield Ok(card.asJson)

    runner.run(res)
  }

  /** Service for querying info about a deck loaded as an xml file */
  def deckStats = Action(parse.multipartFormData) { implicit request =>
    def deleteFile(path: Path): UIO[Unit] =
      (ZIO.attempt(Files.delete(path)) *> ZIO.succeed(log.debug(s"Deleted file $path")))
        .catchAll(ex => ZIO.succeed(log.warn(s"Error deleteing $path", ex)))

    log.info("Call into deckStats")

    val r = request
      .body
      .file("deck")
      .map { content =>
        val file     = Paths.get(content.filename).getFileName()
        val realFile = Paths.get(s"$file-tmp")
        content.ref.moveTo(realFile, replace = true)

        log.debug(s"Got file $file and made a temp in $realFile")

        val res: ZIO[Any, Serializable, DeckObject] =
          for {
            deckLoader <- CardController.xmlDeckLoader(realFile.toFile(), loader, runner)
            deck       <- deckLoader.load()
            deckObject  = DeckCalc.fullCalc(deck)
            _          <- ZIO.succeed(log.debug(s"=> $deckObject"))
          } yield deckObject

        Ok(runner.run(res.ensuring(deleteFile(realFile))).asJson)
      }
      .getOrElse(BadRequest("Missing deck"))

    log.debug(s"r: $r")
    r
  }

  /** Load deckStats with a natural deck loader */
  def naturalDeckStats = Action { implicit request =>
    val r = request.body.asText

    r.fold(BadRequest("No deck")) { text =>
      val res = CardController
        .naturalDeckLoader(text, runner)
        .flatMap(_.load())
        .map(deck => DeckCalc.fullCalc(deck))
      Ok(runner.run(res).asJson)
    }
  }
}

private object CardController {
  private[this] val log = Logger(classOf[CardController])

  /** Card loader a usar para servir el contenido */
  def loader(runner: run.ZioRunner): ZIO[Any, ConfigReaderFailures, CardLoader] =
    for {
      config    <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      _         <- ZIO.succeed(log.info(s"Loaded configuration: $config"))
      ds         = dataSource(config)
      loader     = new MagicIOLoader(config.requestConfig.timeout)
      xmlLoader  = new XMLCardLoader("cards.xml")
      seqLoader  = new SequenceLoader(xmlLoader, loader)
      cardLoader = new H2DBQuillLoader(seqLoader, ds, runner)
      // cardLoader = new CachedLoader(MagicIOLoader)
    } yield cardLoader

  /** Deck loader to process deck requests. Depends on the loader */
  def xmlDeckLoader(
      f: File,
      loader: CardLoader,
      runner: run.ZioRunner
  ): ZIO[Any, ConfigReaderFailures, XMLDeckLoader] =
    ZIO.succeed(new XMLDeckLoader(f, loader))

  /** Natural deck loader. Depends on the loader */
  def naturalDeckLoader(text: String, runner: run.ZioRunner) =
    loader(runner).map(l => NaturalDeckLoader(text, l))

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())

  def dataSource(config: CoreConfig): DataSource = {
    log.info(s"Starting datasource for ${config.dbConnectionUrl}")
    val ds = new JdbcDataSource()
    ds.setURL(config.dbConnectionUrl)
    ds
  }
}
