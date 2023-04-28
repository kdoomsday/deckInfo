package ebarrientos.deckInfo.controller

import zio._
import zio.http._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.config.CoreConfig
import ebarrientos.deckStats.load.MagicIOLoader
import ebarrientos.deckStats.load.XMLCardLoader
import ebarrientos.deckStats.load.SequenceLoader
import ebarrientos.deckStats.load.H2DBQuillLoader
import org.slf4j.LoggerFactory
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource
import ebarrientos.deckStats.run.ZioRunner
import ebarrientos.deckStats.run.ZioRunnerDefault
import ebarrientos.deckStats.basics.Card
import io.circe.generic.auto._
import io.circe.syntax._
import java.net.URLDecoder

object CardController {

  private val log = LoggerFactory.getLogger(CardController.getClass())

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

  /** Card meaning no card found */
  private val nullCard: Card = Card(Seq(), "Not found", Set())

  /** The actual app that will serve requests */
  val app: Http[Any, Response, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "card" / name =>
      val unencodedName = URLDecoder.decode(name, Charsets.Utf8)
      (for {
        loader <- loaderLive
        c      <- loader.card(unencodedName)
      } yield Response.json(c.getOrElse(nullCard).asJson.toString))
        .catchAll { ex =>
          ZIO.succeed(log.error("Error processing", ex)) *>
            ZIO.succeed(
              Response
                .text(s"Error processing. Please consult.")
                .withStatus(Status.InternalServerError)
            )
        }
  }
}
