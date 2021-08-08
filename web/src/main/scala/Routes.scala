package com.ebarrientos.deckInfo.web

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.staticcontent._
import org.http4s.twirl._

import circe._
import io.circe.generic.auto._
import io.circe.syntax._

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
import ebarrientos.deckStats.load.NaturalDeckLoader
import ebarrientos.deckStats.queries.DeckCalc
import ebarrientos.deckStats.basics.Deck

object QueryRoutes {
  private val dsl = new Http4sDsl[Task] {}
  import dsl._
  import RoutesObjects._

  import com.ebarrientos.deckInfo.web.Encoders._

  private val queryCardService = HttpRoutes.of[Task] {
    case GET -> Root / "card" / name => {
      val res = for {
        l   <- loader
        c   <- l.card(name)
        card = c.getOrElse(nullCard)
      } yield Ok(card.asJson)

      res.absorbWith(s => new Throwable("Error: " + s.toString())).flatten
    }
  }

  private val queryCardService2 = HttpRoutes.of[Task] {
    case GET -> Root / "card2" / name => {
      val res = for {
        l   <- loader
        c   <- l.card(name)
        resp = c.map(card => Ok(card.asJson))
                 .getOrElse(BadRequest(s"Unknown card $name"))
      } yield resp

      res.absorbWith(s => new Throwable("Error: " + s.toString())).flatten
    }
  }

  private val naturalDeckStats = HttpRoutes.of[Task] {
    case req @ POST -> Root / "deckN" => {
      req
        .bodyText
        .compile
        .string
        .map(text =>
          RoutesObjects
            .naturalDeckLoader(text)
            .flatMap(_.load())
            .map(deck => DeckCalc.fullCalc(deck))
            .map(stats => Ok(stats.asJson))
            .flatten
        )
        .flatten
    }
  }

  // Twirl routes
  private val twirlRoutes = HttpRoutes.of[Task] {
    case GET -> Root / "index" => {
      Ok(deckInfo.html.index(Seq(1, 2, 3)))
    }
  }

  // val queryApp: HttpApp[Task] = (queryCardService <+> queryCardService2).orNotFound
  def queryApp(blocker: Blocker): HttpApp[Task] =
    (queryCardService
       <+> queryCardService2
       <+> naturalDeckStats
       <+> twirlRoutes
       <+> fileService(FileService.Config("site", blocker))).orNotFound
}

private object RoutesObjects {

  /** Card loader a usar para servir el contenido */
  val loader: ZIO[Any, ConfigReaderFailures, CardLoader] = {
    for {
      config    <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      ec         = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
      cardLoader = new H2DBDoobieLoader(MagicIOLoader, config, ec)
    } yield cardLoader
  }

  /** Natural deck loader. Depends on the loader */
  def naturalDeckLoader(
      text: String
  ): ZIO[Any, Throwable, NaturalDeckLoader] =
    loader
      .map(l => NaturalDeckLoader(text, l))
      .absorbWith(f => new Throwable("Error: " + f.toString()))

  /** Carta que indica que nada se consiguio */
  val nullCard: Card = Card(Seq(), "Not found", Set())
}
