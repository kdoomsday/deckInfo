package controllers

import zio.ZIO

/** Runner for ZIO values */
trait ZioRunner {
  def runtime: zio.Runtime[Any]

  /** Execute a ZIO inside this runners Runtime
    *
    * @param z
    * @return
    */
  def run[E, A](z: ZIO[Any, E, A]): A =
    runtime.unsafeRun(z)
}

