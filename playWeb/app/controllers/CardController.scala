package controllers

import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.mvc.Action
import play.api.mvc.BaseController
import play.api.mvc.ControllerComponents
import javax.inject.Inject
import zio.ZIO
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ebarrientos.deckStats.config.CoreConfig
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import ebarrientos.deckStats.load.MagicIOLoader
import ebarrientos.deckStats.load.H2DBDoobieLoader
import ebarrientos.deckStats.queries.{ DeckCalc, DeckObject }
import pureconfig.error.ConfigReaderFailures
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.basics.Card
import play.api.libs.json.Json
import io.circe.generic.auto._
import io.circe.syntax._
import play.api.http.Writeable
import play.api.libs.circe.Circe
import play.api.Logger
import com.fasterxml.jackson.core.PrettyPrinter
import ebarrientos.deckStats.load.XMLDeckLoader
import java.io.File
import ebarrientos.deckStats.load.DeckLoader
import ebarrientos.deckStats.math.Calc
import ebarrientos.deckStats.basics.Land
import java.nio.file.Paths
import java.nio.file.Files
import play.api.mvc.MultipartFormData
import play.api.mvc.Result

class CardController @Inject() (
    val controllerComponents: ControllerComponents,
    runner: ZioRunner
) extends BaseController
    with Circe {

  val log = Logger(getClass())

  def card(name: String) = Action { implicit request: Request[AnyContent] =>
    val res: ZIO[Any,Serializable,Result] = for {
      l   <- CardController.loader
      c   <- l.card(name)
      card = c.getOrElse(CardController.nullCard)
    } yield Ok(card.asJson)

    runner.run(res)
  }

  /** Service for querying info about a deck loaded as an xml file */
  def deckStats = Action(parse.multipartFormData) { implicit request =>
    log.info("Call into deckStats")

    val r = request
      .body
      .file("deck")
      .map { content =>
        val file     = Paths.get(content.filename).getFileName()
        val realFile = Paths.get(s"/tmp/$file")
        content.ref.moveTo(realFile, replace = true)

        log.debug(s"Got file $file")

        val res: ZIO[Any, Serializable, DeckObject] =
          for {
            deckLoader <- CardController.xmlDeckLoader(realFile.toFile())
            deck       <- deckLoader.load()
            deckObject  = { val dobj = DeckCalc.fullCalc(deck); log.debug(s"=> $dobj" ); dobj }
          } yield deckObject

        Ok(runner.run(res).asJson)
      }
      .getOrElse(BadRequest("Missing deck"))

    log.debug(s"r: $r")
    r
  }
}

private object CardController {

  /** Card loader a usar para servir el contenido */
  val loader: ZIO[Any, ConfigReaderFailures, CardLoader] = {
    for {
      config    <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      ec         = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
      cardLoader = new H2DBDoobieLoader(MagicIOLoader, config, ec)
    } yield cardLoader
  }

  /** Deck loader to process deck requests. Depends on the loader */
  def xmlDeckLoader(f: File): ZIO[Any, ConfigReaderFailures, XMLDeckLoader] =
    loader.map(l => new XMLDeckLoader(f, l))

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())
}
