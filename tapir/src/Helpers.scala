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
    ZIO.succeed {
      val timeout    = appConfig.requestConfig.timeout
      val retryTime  = appConfig.requestConfig.retryTime
      val maxRetries = appConfig.requestConfig.maxRetries
      val ds         = dataSource(appConfig)
      val loader     = new MagicIOLoader(timeout, retryTime, maxRetries)
      val xmlLoader  = new XMLCardLoader(appConfig.paths.xmlCards)
      val seqLoader  = new SequenceLoader(xmlLoader, loader)
      val cardLoader = new H2DBQuillLoader(seqLoader, ds, appConfig.paths.initScripts, runner)
      cardLoader
    }

  val appConfig: CoreConfig = ConfigSource.default.loadOrThrow[CoreConfig]

  val loaderLive: CardLoader =
    runner.run(
      loaderZIO(appConfig, runner)
        .mapError(configErrorFailures => new Exception(configErrorFailures.prettyPrint()))
        .orDie
    )

  val logicLive = new ZIOServerLogic(loaderLive)
}
