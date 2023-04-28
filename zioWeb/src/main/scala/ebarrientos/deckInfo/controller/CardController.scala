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

  /** The actual app that will serve requests */
  val app: Http[CardLoader, Response, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "card" / name =>
      val unencodedName = URLDecoder.decode(name, Charsets.Utf8)
      (for {
        loader <- ZIO.service[CardLoader]
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


  /** Card meaning no card found */
  private val nullCard: Card = Card(Seq(), "Not found", Set())
}
