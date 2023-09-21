package ebarrientos.deckStats.run

import javax.inject.{ Singleton, Inject }
import zio.Unsafe

/** Default ZioRunner that uses [[zio.Runtime.default]] */
@Singleton
class ZioRunnerDefault @Inject() (implicit override val unsafe: Unsafe) extends ZioRunner {
  val runtime = zio.Runtime.default
}
