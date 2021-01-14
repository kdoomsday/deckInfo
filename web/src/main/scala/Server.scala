package com.ebarrientos.deckInfo.web

import cats.effect.Blocker

import org.http4s.server.blaze.BlazeServerBuilder

import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.effect.Resource

object Server extends App {

  private def mkServer(blocker: Blocker): ZIO[Any, Throwable, Unit] =
    ZIO.runtime[Any].flatMap(implicit rts =>
      BlazeServerBuilder[Task]
        .bindHttp(8080, "localhost")
        .withHttpApp(QueryRoutes.queryApp(blocker))
        .serve
        .compile
        .drain
    )

  val server: Resource[Task, ZIO[Any, Throwable, Unit]] =
    for {
      blocker <- Blocker[Task]
    } yield mkServer(blocker)

  def run(args: List[String]) =
    // server.use(_.fold(_ => ExitCode(1), _ => ExitCode(0)))
    server.use(_.exitCode).catchAll(_ => Task.succeed(ExitCode(2)))
}
