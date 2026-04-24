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


  /**
   * Race a primary effect against a sequence of backups, introducing each backup
   * progressively.
   *
   * This composes `raceBackup` repeatedly so the overall effect tries `z` first
   * and then races the result against `zs` one by one. Each call to
   * `raceBackup` uses an adjusted delay so later backups are introduced after
   * proportionally larger waits. Effect 2 waits `delay` before starting. Effect 3
   * waits `delay * 2`, and so forth.
   *
   * Behavior and guarantees:
   * - The returned effect completes with the first non-`None` `Option[A]` produced
   *   by any of the provided effects.
   * - Each backup is introduced using `raceBackup`, so a backup is started at the
   *   earliest of (a) the left side failing, (b) the left side producing `None`,
   *   or (c) the configured delay for that backup. `raceBackup` also guarantees a
   *   given backup effect is started at most once.
   * - If every effect returns `None`, the result is `None`.
   * - The overall effect only fails if every effect fails; which failure is
   *   reported is unspecified (same semantics as `raceBackup` composition).
   *
   * Concurrency and interruption:
   * - Internally this builds a chain of races. When a winner completes, losers
   *   are interrupted according to ZIO's race semantics (and `raceBackup`'s
   *   internal semantics).
   *
   * Example:
   * {{{
   *   SmartRace.raceAllBackup(primary, backup1, backup2)(delay)
   * }}}
   *
   * @param z   Primary effect to try first (must produce an Option[A])
   * @param zs  Additional backup effects; each is tried in order
   * @param delay Base delay used to stagger introduction of successive backups
   * @return    A `ZIO[R, E, Option[A]]` with the first non-`None` result or
   *            `None` if none of the effects produced a value; fails only if all
   *            effects fail.
   */
  def raceAllBackup[R, E, A](z: ZIO[R, E, Option[A]], zs: ZIO[R, E, Option[A]]*)(
      delay: Duration
  ): ZIO[R, E, Option[A]] =
      /**
       * Recursive run that repeatedly uses raceBackup to race all effects.
       *
       * @param iteration Which iteration we are in, to adjust the delay for further effects
       * @param initial First effect to race
       * @param rest All other effects to race
       */
      def rb(
          iteration: Int,
          initial: ZIO[R, E, Option[A]],
          rest: ZIO[R, E, Option[A]]*
      ): ZIO[R, E, Option[A]] =
        if rest.isEmpty then initial
        else rb(iteration + 1, raceBackup(initial, rest.head)(delay * iteration), rest.tail*)

      rb(1, z, zs*)

}
