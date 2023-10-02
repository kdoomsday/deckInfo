package ebarrientos.deckInfo

import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.run.ZioRunner
import zio._
import pureconfig.error.ConfigReaderFailures
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.load.MagicIOLoader
import ebarrientos.deckStats.load.XMLCardLoader
import ebarrientos.deckStats.load.SequenceLoader
import ebarrientos.deckStats.load.H2DBQuillLoader
import ebarrientos.deckStats.run.ZioRunnerDefault
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Helpers {

  private val runner: ZioRunner =
    Unsafe.unsafe { implicit unsafe =>
      new ZioRunnerDefault()
    }

  private def dataSource(config: CoreConfig): DataSource = {
    val ds = new JdbcDataSource()
    ds.setURL(config.dbConnectionUrl)
    ds
  }

  /** Card loader a usar para servir el contenido */
  private def loaderZIO(
      appConfig: CoreConfig,
      runner: ZioRunner
  ): ZIO[Any, ConfigReaderFailures, CardLoader] =
    for {
      ds         <- ZIO.succeed(dataSource(appConfig))
      loader     <-
        ZIO.succeed(
          new MagicIOLoader(appConfig.requestConfig.timeout, appConfig.requestConfig.retryTime)
        )
      xmlLoader  <- ZIO.succeed(new XMLCardLoader(appConfig.paths.xmlCards))
      seqLoader  <- ZIO.succeed(new SequenceLoader(xmlLoader, loader))
      cardLoader <-
        ZIO.succeed(new H2DBQuillLoader(seqLoader, ds, appConfig.paths.initScripts, runner))
    } yield cardLoader

  val appConfig: CoreConfig = ConfigSource.default.loadOrThrow[CoreConfig]

  val loaderLive: CardLoader =
    runner.run(
      loaderZIO(appConfig, runner)
        .mapError(configErrorFailures => new Exception(configErrorFailures.prettyPrint()))
        .orDie
    )

  val logicLive = new ZIOServerLogic(loaderLive)
}
