package com.ebarrientos.deckInfo.web

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._

import zio.Task
import zio.interop.catz._
import zio.interop.catz.implicits._

object QueryRoutes {
  private val dsl = new Http4sDsl[Task]{}
  import dsl._

  private val queryCardService = HttpRoutes.of[Task] {
    case GET -> Root / "card" / name => {
      Ok(s"Hello, ${name}")
    }
  }


  val queryApp: HttpApp[Task] = queryCardService.orNotFound
}
