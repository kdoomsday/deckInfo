package ebarrientos.deckStats

import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.Legendary
import ebarrientos.deckStats.basics.{ColoredMana, GenericMana}
import ebarrientos.deckStats.basics.{Artifact, Creature, Land}
import ebarrientos.deckStats.basics.{Black, Blue, Green, Red, White}
import ebarrientos.deckStats.load.CardLoader
import zio.IO
import ebarrientos.deckStats.basics.HybridMana

/** Dummy objects to use in tests */
object DummyObjects {

  /* Cards */
  val arthur = Card(
    Seq(ColoredMana(White)),
    "Arthur Dent",
    Set(Creature),
    Set(Legendary),
    Set("Homeowner"),
    "text",
    1,
    1
  )

  val trillian = Card(
    Seq(HybridMana(Set(ColoredMana(Blue), ColoredMana(Red)))),
    "Tricia McMillan",
    Set(Creature),
    Set(Legendary),
    Set("Space Adventurer"),
    "text",
    1,
    2
  )

  val ford = Card(
    Seq(ColoredMana(Red), ColoredMana(Red)),
    "Ford Prefect",
    Set(Creature),
    Set(Legendary),
    Set("Hitchhiker"),
    "text",
    2,
    2
  )

  val zaphod = Card(
    Seq(GenericMana(1), ColoredMana(Black)),
    "Zaphod BeebleBrox",
    Set(Creature),
    Set(Legendary),
    Set("Space Adventurer"),
    "text",
    2,
    1
  )

  val marvin = Card(
    Seq(GenericMana(3)),
    "Marvin the Paranoid Android",
    Set(Artifact, Creature),
    Set(Legendary),
    Set("Android"),
    "1t: Tap target Creature",
    0,
    100
  )

  val restaurant = Card(
    Seq(),
    "The Restaurant at the End of the Universe",
    Set(Land),
    Set(Legendary),
    Set(),
    "t: Add 2 generic mana to your mana pool",
    0,
    0
  )

  val heartOfGold = Card(
    Seq(
      ColoredMana(White),
      ColoredMana(Blue),
      ColoredMana(Black),
      ColoredMana(Red),
      ColoredMana(Green)
    ),
    "The Heart of Gold",
    Set(Artifact),
    Set(Legendary),
    Set(),
    """|(5): Flip a coin. Heads, you win the game.
                            |Tails you lose the game""".stripMargin,
    0,
    0
  )

  /** Dummy deck loader to be used for tests */
  val dummyCardLoader: CardLoader = new CardLoader {
    def card(name: String): IO[Throwable, Option[Card]] =
      IO.succeed(name match {
        case "Arthur Dent"                               => Some(arthur)
        case "Tricia McMillan"                           => Some(trillian)
        case "Ford Prefect"                              => Some(ford)
        case "Zaphod BeebleBrox"                         => Some(zaphod)
        case "Marvin the Paranoid Android"               => Some(marvin)
        case "The Restaurant at the End of the Universe" => Some(restaurant)
        case "The Heart of Gold"                         => Some(heartOfGold)
        case _                                           => None
      })
  }
}
