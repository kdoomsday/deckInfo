package ebarrientos.deckStats.run


import zio._
import java.{util => ju}


object SmartRace {
  private case object NoneFailure


  /**
   * Races z1 and z2 to get a value.
   *
   * The effect completes with a value if one of the two combined effects returns a value.
   *    - Result will be the first response that is not a None
   *    - z2 starts at the earliest of z1 failing, z1 finishing with a None, startDelay
   *    - If both results are None, returns None
   *    - If one is a None and one a failure, will return None
   *    - Will only fail if both effects fail. Which failure is reported is unspecified
   *
   * TODO This still does not start z2 early if z1 fails or does not find.
   *
   * @param z1 First effect
   * @param z2 Second effect
   * @param startDelay How long to wait for z1 before starting z2
   * @return The fetched result.
   */
  def race[R, E1, E2, A](z1: ZIO[R, E1, Option[A]], z2: ZIO[R, E2, Option[A]])(
      startDelay: Duration
  ): ZIO[R, E1 | E2, Option[A]] =
    val l = z1.someOrFail(NoneFailure)
    val r = (ZIO.sleep(startDelay) *> z2).someOrFail(NoneFailure)

    l.race(r)
      .asSome
      .catchSome { case NoneFailure => ZIO.succeed(None) }
      .asInstanceOf[ZIO[R, E1 | E2, Option[A]]] // force fit to expected type

}
