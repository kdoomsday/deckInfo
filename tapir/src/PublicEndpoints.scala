package ebarrientos.deckInfo

import sttp.tapir.ztapir._
import sttp.tapir.files._
import zio.Task

object PublicEndpoints {
  val publicServe: ZServerEndpoint[Any, Any] = staticFilesGetServerEndpoint[Task](emptyInput)("www")

  val all = List(publicServe)
}
