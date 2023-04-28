package ebarrientos.deckInfo

import zio._
import zio.http.Server
import java.io.IOException
import ebarrientos.deckInfo.controller.CardController
import zio.http.{Http, Request, Response}

object ZIOServer extends ZIOAppDefault {

  override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] = {
    val app = CardController.app ++ HelloApp.app

    val config = Server.Config.default.port(9000)
    val configLayer = ZLayer.succeed(config)
    Server.serve(app).provide(configLayer, Server.live)
  }

}

