package com.ebarrientos.deckInfo.web

import zio.App
import zio.ZIO
import zio.Task
import org.http4s.server.blaze.BlazeServerBuilder

import zio.Runtime.global
import zio.interop.catz._
import zio.interop.catz.implicits._

object Server extends App {

  val server = ZIO.runtime[Any]
    .flatMap {
      implicit rts =>
      BlazeServerBuilder[Task]
        .bindHttp(8080, "localhost")
        .withHttpApp(QueryRoutes.queryApp)
        .serve
        .compile
        .drain
    }

  def run(args: List[String]) =
    server.fold(_ => 1, _ => 0)

}
