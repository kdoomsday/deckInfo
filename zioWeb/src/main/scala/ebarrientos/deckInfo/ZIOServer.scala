package ebarrientos.deckInfo

import zio._
import zio.http.Server
import java.io.IOException
import ebarrientos.deckInfo.controller.{ CardController, PublicController }
import zio.http._
import ebarrientos.deckStats.run.ZioRunner
import ebarrientos.deckStats.run.ZioRunnerDefault
import ebarrientos.deckStats.config.CoreConfig
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource
import ebarrientos.deckStats.load.CardLoader
import pureconfig.error.ConfigReaderFailures
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ebarrientos.deckStats.basics.Card
import org.slf4j.LoggerFactory
import ebarrientos.deckStats.load.MagicIOLoader
import ebarrientos.deckStats.load.XMLCardLoader
import ebarrientos.deckStats.load.SequenceLoader
import ebarrientos.deckStats.load.H2DBQuillLoader
import java.io.File

object ZIOServer extends ZIOAppDefault {

  private val log = LoggerFactory.getLogger(getClass())

  override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] = {
    val app = CardController.app ++ PublicController.app

    val config = Server.Config.default.port(9000)
    val configLayer = ZLayer.succeed(config)
    val loaderLayer = ZLayer.fromZIO(loaderLive)
    Server.serve(app).provide(loaderLayer, configLayer, Server.live)
  }

  /** Dependencies */
  private val runner: ZioRunner =
    Unsafe.unsafe { implicit unsafe =>
      new ZioRunnerDefault()
    }

  private val loaderLive: URIO[Any, CardLoader] =
    loaderZIO(runner)
      .orDieWith { configReaderFailures =>
        val ex = new Exception(s"Error: $configReaderFailures")
        log.error("Error creating loader", ex)
        ex
      }

  private def dataSource(config: CoreConfig): DataSource = {
    log.info(s"Starting datasource for ${config.dbConnectionUrl}")
    val ds = new JdbcDataSource()
    ds.setURL(config.dbConnectionUrl)
    ds
  }

  /** Card loader a usar para servir el contenido */
  private def loaderZIO(runner: ZioRunner): ZIO[Any, ConfigReaderFailures, CardLoader] =
    for {
      config    <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      _         <- ZIO.succeed(log.info(s"Loaded configuration: $config"))
      ds        <- ZIO.succeed(dataSource(config))
      loader    <- ZIO.succeed(new MagicIOLoader(config.requestConfig.timeout, config.requestConfig.retryTime))
      xmlLoader <- ZIO.succeed(new XMLCardLoader(config.paths.xmlCards))
      seqLoader <- ZIO.succeed(new SequenceLoader(xmlLoader, loader))
      cardLoader<- ZIO.succeed(new H2DBQuillLoader(seqLoader, ds, config.paths.initScripts, runner))
    } yield cardLoader
}

