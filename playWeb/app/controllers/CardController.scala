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
import models.DeckObject
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


  // def deckStats(deck: String) = Action { implicit request: Request[AnyContent] =>
  //   Ok(views.html.deck(deck.length()))
  // }
  def deckStats = Action(parse.multipartFormData) { implicit request =>
    log.info("Call into deckStats")

    request.body.file("deck").map { content =>
      val fileSize = content.fileSize
      val res = DeckObject(fileSize)

      Ok(res.asJson)
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

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())
}
