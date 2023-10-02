package ebarrientos.deckInfo

import sttp.tapir._
import zio.ZIO
import sttp.tapir.ztapir.ZServerEndpoint

object Endpoints {

  // val helloEndpoint = Endpoint[Any, Unit, Unit, String, Any].get.path("hello").out("Hello")
  val helloEndpoint: Endpoint[Unit, Unit, Unit, String, Any] =
    endpoint.in("hello").get.out(stringBody("utf-8"))

  val helloServerEndpoint: ZServerEndpoint[Any, Any]         =
    helloEndpoint.serverLogicSuccess(_ => ZIO.succeed("Hello!"))


  val allEndpoints = List(helloServerEndpoint)
}
