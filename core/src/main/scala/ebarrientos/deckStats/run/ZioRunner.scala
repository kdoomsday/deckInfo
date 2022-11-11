package ebarrientos.deckStats.run

import zio.ZIO
import zio.Unsafe

/** Runner for ZIO values */
trait ZioRunner {
  def runtime: zio.Runtime[Any]
  implicit def unsafe: Unsafe

  /** Execute a ZIO inside this runners Runtime
    *
    * @param z
    * @return
    */
  def run[E, A](z: ZIO[Any, E, A]): A =
    runtime.unsafe.run(z).getOrThrowFiberFailure()
}

