package ebarrientos.deckInfo

import zio.http._

object HelloApp {
  val app = Http.collect[Request] {
    case Method.GET -> !! / "hello" => Response.text("Hello")
  }
}

