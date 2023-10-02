package ebarrientos.deckInfo

import sttp.tapir.ztapir._
import sttp.tapir.files._
import zio.Task

object PublicEndpoints {
  private val fileOptions: FilesOptions[Task] =
    FilesOptions
      .default
      .fileFilter(!_.exists(_ == "docs"))

  // val assetsServe: ZServerEndpoint[Any, Any] = staticFilesGetServerEndpoint[Task]("assets")("www/assets", fileOptions)
  // val publicServe: ZServerEndpoint[Any, Any] = staticFileGetServerEndpoint(emptyInput)("www/index.html")
  val publicServe: ZServerEndpoint[Any, Any] = staticFilesGetServerEndpoint[Task](emptyInput)("www", fileOptions)

  val all = List(publicServe)
}
