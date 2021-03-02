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
import ebarrientos.deckStats.queries.{ DeckObject, DeckCalc }
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
    val res = for {
      l   <- CardController.loader
      c   <- l.card(name)
      card = c.getOrElse(CardController.nullCard)
    } yield Ok(card.asJson)

    runner.run(res)
  }


  /** Service for querying info about a deck loaded as an xml file */
  def deckStats = Action(parse.multipartFormData) { implicit request =>
    log.info("Call into deckStats")

    // val file = Files.createTempFile("~/tmp.xml", null, null)
    // request.body.moveTo(file, replace = true)
    // val res = for {
    //   deckLoader <- CardController.xmlDeckLoader(file.toFile())
    //   deck <- deckLoader.load
    //   (avg, avgNL) = (Calc.avgManaCost(deck), Calc.avgManaCost(deck, _.is(Land)))
    //   deckObject = DeckObject(avg, avgNL)
    // } yield Ok(deckObject.asJson)


    request.body.file("deck").map { content =>
      val file = Paths.get(content.filename).getFileName()
      val realFile = Paths.get(s"/tmp/$file")
      content.ref.moveTo(realFile, replace = true)

      log.debug(s"Got file $file")

      val res: ZIO[Any,Serializable,Result] =
        for {
          deckLoader <- CardController.xmlDeckLoader(realFile.toFile())
          deck       <- deckLoader.load()
          // (avg, avgNL) = (Calc.avgManaCost(deck), Calc.avgManaCost(deck, _.is(Land)))
          deckObject  = DeckCalc.fullCalc(deck)
          // deckObject = DeckObject(avg, avgNL)
        } yield Ok(deckObject.asJson)

      runner.run(res)
    }
    .getOrElse(BadRequest("Missing deck"))

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

  def xmlDeckLoader(f: File): ZIO[Any, ConfigReaderFailures, XMLDeckLoader]  =
    loader.map(l => new XMLDeckLoader(f, l))

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())
}
