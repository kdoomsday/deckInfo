package ebarrientos.deckStats.load.utils

import scala.util.Try
import zio._


/** Utilities for loaders that utilize URLs. */
trait URLUtils {

  /** Sanitize a string for use in a URL. */
  def sanitize(str: String): String = str.replace(" ", "%20").replace("'", "%27").trim()

  /** Read a URL into a String. Sanitizes the url before making the request.*/
  def readURL(url: String): String = scala.io.Source.fromURL(sanitize(url)).mkString

  /** Read URL into String, wrapping the operation in an IO.
    * It also uses a Try, so exceptions are caught and must be handled through
    * the IO.
    */
  def ioReadUrl(url: String): Task[String] =
    ZIO.fromTry( Try(readURL(url)) )
}
