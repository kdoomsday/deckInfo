package ebarrientos.deckStats.load.utils

/** Utilities for loaders that utilize URLs. */
trait URLUtils {

  /** Sanitize a string for use in a URL. */
  def sanitize(str: String): String = str.replace(" ", "%20").trim()

  /** Read a URL into a String. Sanitizes the url before making the request.*/
  def readURL(url: String) = scala.io.Source.fromURL(sanitize(url)).mkString
}
