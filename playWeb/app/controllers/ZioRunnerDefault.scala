package controllers

import javax.inject.Singleton

/** Default ZioRunner that uses [[zio.Runtime.default]] */
@Singleton
class ZioRunnerDefault extends ZioRunner {
  val runtime = zio.Runtime.default
}
