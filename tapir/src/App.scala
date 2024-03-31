package ebarrientos.deckInfo

import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.ztapir._
import zio._
import zio.http.Server
import pureconfig.ConfigSource
import ebarrientos.deckStats.config.CoreConfig
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import org.slf4j.LoggerFactory
import org.slf4j.Logger

object App extends ZIOAppDefault {
  private val appConfig: CoreConfig = ConfigSource.default.loadOrThrow[CoreConfig]

  private val log: Logger = LoggerFactory.getLogger(classOf[App])

  override def run = {
    val serverOptions: ZioHttpServerOptions[Any] =
      ZioHttpServerOptions.customiseInterceptors.options

    val port = appConfig.port

    val cardEnpoints = CardServerEndpoints.allEndpoints
    val publicEndpoints = PublicEndpoints.all
    val endpoints = cardEnpoints ++ publicEndpoints

    val docEndpoints: List[ZServerEndpoint[Any, Any]] =
      SwaggerInterpreter().fromServerEndpoints[Task](endpoints, "deckInfo", "1.0.0")

    (
      for {
        _            <- zio.Console.printLine("Starting up server...")
        allEndpoints <- ZIO.succeed(docEndpoints ++ endpoints)
        app          <- ZIO.succeed(ZioHttpInterpreter(serverOptions).toHttp(allEndpoints))
        actualPort   <- Server.install(app)
        _            <- zio.Console.printLine(welcomeMessage(actualPort))
        _            <- zio.Console.readLine
        _            <- ZIO.succeed(log.info("Server shutdown requested"))
      } yield ()
    ).provide(
      Server.defaultWithPort(port)
    ).exitCode
  }

  private def welcomeMessage(port: Int): String =
    s"""|Welcome to DeckInfo!
        |Go to http://localhost:${port} for the application.
        |Go to http://localhost:${port}/docs to open SwaggerUI.
        |Press ENTER key to exit.""".stripMargin
}
