package ebarrientos.deckInfo.controller

import zio.http._
import org.slf4j.LoggerFactory
import zio.stream.ZStream
import java.io.FileNotFoundException

/** Controller for public resources like html, javascript, and such */
object PublicController {

  private val log = LoggerFactory.getLogger(getClass())

  val app = Http.collectHttp[Request] {
    case Method.GET -> !! / "assets" / "stylesheets" / style =>
      Handler.fromStream(getResource("public/stylesheets", style)).toHttp

    case Method.GET -> !! / "assets" / "js" / script =>
      Handler.fromStream(getResource("public/js", script)).toHttp

    case Method.GET -> !! / filename =>
      log.debug(s"Serving public file: $filename")
      Handler.fromStream(getResource("public", filename)).toHttp

    case Method.GET -> !! =>
      log.debug("Requested index page")
      Handler.fromStream(getResource("public", "plainIndex.html")).toHttp

  }.withDefaultErrorResponse

  private def getResource(root: String, name: String) = {
    val resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(s"$root/$name")
    if (resource == null) {
      log.error(s"Could not find resource")
      ZStream.fail(new FileNotFoundException(name))
    }
    else {
      // log.warn(resource.toURI().toString())
      // log.warn(resource.toURI().getRawPath())
      ZStream.fromInputStream(resource)
    }
  }
}
