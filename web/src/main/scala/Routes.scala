package com.ebarrientos.deckInfo.web

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._

import circe._
import io.circe.generic.auto._
import io.circe.syntax._
// import circe._
// import io.circe.generic.semiauto._
// import io.circe.syntax._

import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits._
import pureconfig.ConfigSource
import ebarrientos.deckStats.config.CoreConfig
import zio.ZIO
import scala.concurrent.ExecutionContext
import ebarrientos.deckStats.load.H2DBDoobieLoader
import ebarrientos.deckStats.load.MagicIOLoader
import java.util.concurrent.Executors
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.basics.Card

object QueryRoutes {
  private val dsl = new Http4sDsl[Task]{}
  import dsl._
  import RoutesObjects._

  import com.ebarrientos.deckInfo.web.Encoders._

  private val queryCardService = HttpRoutes.of[Task] {
    case GET -> Root / "card" / name => {
      val res = for {
        l <- loader
        c <- l.card(name)
        card = c.getOrElse(nullCard)
      } yield Ok(card.asJson)

      res.absorbWith(s => new Throwable("Error: " + s.toString()))
         .flatten
    }
  }

  val queryApp: HttpApp[Task] = queryCardService.orNotFound
}

private object RoutesObjects {
  /** Card loader a usar para servir el contenido */
  val loader: ZIO[Any, ConfigReaderFailures, CardLoader] = {
    for {
      config     <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      ec          = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
      cardLoader  = new H2DBDoobieLoader(MagicIOLoader, config, ec)
    } yield cardLoader
  }

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())

  // implicit val cardencoder: Encoder[Card] = deriveEncoder
}