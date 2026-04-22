package ebarrientos.deckStats.run


import zio._
import java.{util => ju}


object SmartRace {
  private case object NoneFailure
  private case object StartedFailure


  /**
   * Attempts to get a value from `z1` using `z2` as backup. It will try to
   * avoid starting `z2` if possible, but also start it immediately if it is
   * already known `z1` will not produce a result
   *
   * More formally, the behavior is `z1` will be started. If `z1` fails,
   * `startDelay` has ellapsed, or no value is found, `z2` will be started. `z2`
   * will never be started more than once. If at any point `z1` and `z2` are
   * both running, whichever is first will win.
   *
   * The effect completes with a value if one of the two effects returns a value. Some properties:
   *   - Result will be the first response that is not a None
   *   - `z2` starts at the earliest of `z1` finishing with a `None`, `z1` failing, `startDelay`
   *   - If both results are `None`, returns `None`
   *   - If one is a `None` and one a failure, will return `None`
   *   - Will only fail if both effects fail. Which failure is reported is unspecified
   *
   * @param z1 First effect
   * @param z2 Second effect
   * @param startDelay How long to wait for z1 before starting z2
   * @return The fetched result.
   */
  def raceBackup[R, E1, E2, A](z1: ZIO[R, E1, Option[A]], z2: ZIO[R, E2, Option[A]])(
      startDelay: Duration
  ): ZIO[R, E1 | E2, Option[A]] =
    Ref.make(false).flatMap { z2Started =>
        val z2Once = z2Started.modify { started =>
          if (started) (ZIO.fail(StartedFailure), true)
          else (z2, true)
        }.flatten

        val lside = z1
          .flatMap:
              case Some(value) => ZIO.some(value)
              case None        => z2Once.catchAll(_ => ZIO.none) // If already none, failures become None
          .catchAll(_ => z2Once) // Prevents fast z1 failure and z2 None from becoming failure
        val rside = (ZIO.sleep(startDelay) *> z2Once.someOrFail(NoneFailure)).map(Option.apply)

        lside
          .disconnect
          .race(rside.disconnect)
          .catchSome { case NoneFailure => ZIO.none } // It can't be StartFailure on both sides
          .asInstanceOf[ZIO[R, E1 | E2, Option[A]]]
    }

}
