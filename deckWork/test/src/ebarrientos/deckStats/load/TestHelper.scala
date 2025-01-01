package ebarrientos.deckStats

import zio.Unsafe
import zio.ZIO

object TestHelper {
  def run[A](z: ZIO[Any, Throwable, A]): A =
    Unsafe.unsafe(implicit unsafe => zio.Runtime.default.unsafe.run(z).getOrThrow())
}

