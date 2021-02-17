package ebarrientos.deckStats

import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.Legendary
import ebarrientos.deckStats.basics.{ ColoredMana, GenericMana }
import ebarrientos.deckStats.basics.{ Creature, Land, Artifact }
import ebarrientos.deckStats.basics.{ White, Blue, Black, Red, Green }

/** Dummy objects to use in tests */
object DummyObjects {

  /* Cards */
  val arthur = Card(Seq(ColoredMana(White)),
                    "Arthur Dent",
                    Set(Creature),
                    Set(Legendary),
                    Set("Homeowner"),
                    "text",
                    1, 1)

  val trillian = Card(Seq(ColoredMana(Blue)),
                      "Tricia McMillan",
                      Set(Creature),
                      Set(Legendary),
                      Set("Space Adventurer"),
                      "text",
                      1, 2)

  val ford = Card(Seq(ColoredMana(Red), ColoredMana(Red)),
                  "Ford Prefect",
                  Set(Creature),
                  Set(Legendary),
                  Set("Hitchhiker"),
                  "text",
                  2, 2)

  val zaphod = Card(Seq(GenericMana(1), ColoredMana(Black)),
                    "Zaphod BeebleBrox",
                    Set(Creature),
                    Set(Legendary),
                    Set("Space Adventurer"),
                    "text",
                    2, 1)

  val marvin = Card(Seq(GenericMana(3)),
                    "Marvin the Paranoid Android",
                    Set(Artifact, Creature),
                    Set(Legendary),
                    Set("Android"),
                    "1t: Tap target Creature",
                    0, 100)

  val restaurant = Card(Seq(),
                        "The Restaurant at the End of the Universe",
                        Set(Land),
                        Set(Legendary),
                        Set(),
                        "t: Add 2 generic mana to your mana pool",
                        0, 0)
}
