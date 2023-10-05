package ebarrientos.deckInfo

import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.ztapir._
import zio._
import zio.http.Server
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ebarrientos.deckStats.config.CoreConfig
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import org.slf4j.LoggerFactory

object App extends ZIOAppDefault {
  private val appConfig = ConfigSource.default.loadOrThrow[CoreConfig]

  private val log = LoggerFactory.getLogger(classOf[App])

  override def run = {
    val serverOptions: ZioHttpServerOptions[Any] =
      ZioHttpServerOptions.customiseInterceptors.options

    val port = appConfig.port

    val endpoints = CardServerEndpoints.allEndpoints ++ PublicEndpoints.all
    val docEndpoints: List[ZServerEndpoint[Any, Any]] =
      SwaggerInterpreter().fromServerEndpoints[Task](endpoints, "deckInfo", "1.0.0")

    (
      for {
        allEndpoints <- ZIO.succeed(docEndpoints ++ endpoints)
        app          <- ZIO.succeed(ZioHttpInterpreter(serverOptions).toHttp(allEndpoints))
        actualPort   <- Server.install(app.withDefaultErrorResponse)
        _            <-
          zio
            .Console
            .printLine(
              s"Go to http://localhost:${actualPort}/docs to open SwaggerUI. Press ENTER key to exit."
            )
        _            <- zio.Console.readLine
        _            <- ZIO.succeed(log.info("Server shutdown requested"))
      } yield ()
    ).provide(
      Server.defaultWithPort(port)
    ).exitCode
  }

}
