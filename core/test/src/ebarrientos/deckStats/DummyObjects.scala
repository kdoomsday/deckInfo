package ebarrientos.deckStats


import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.{ColoredMana, GenericMana}
import ebarrientos.deckStats.basics.CardType.{Artifact, Creature, Land}
import ebarrientos.deckStats.basics.Color.*
import ebarrientos.deckStats.basics.HybridMana
import ebarrientos.deckStats.basics.Supertype


/** Dummy objects to use in tests */
object DummyObjects {

  /* Cards */
  val arthur = Card(
    Seq(ColoredMana(White)),
    "Arthur Dent",
    Set(Creature),
    Set(Supertype.Legendary),
    Set("Homeowner"),
    "text",
    1,
    1,
    multiverseId = Some(1)
  )


  val trillian = Card(
    Seq(HybridMana(Set(ColoredMana(Blue), ColoredMana(Red)))),
    "Tricia McMillan",
    Set(Creature),
    Set(Supertype.Legendary),
    Set("Human", "Adventurer"),
    "text",
    1,
    2,
    multiverseId = Some(2)
  )


  val ford = Card(
    Seq(ColoredMana(Red), ColoredMana(Red)),
    "Ford Prefect",
    Set(Creature),
    Set(Supertype.Legendary),
    Set("Hitchhiker"),
    "text",
    2,
    2,
    multiverseId = Some(3)
  )


  val zaphod = Card(
    Seq(GenericMana(1), ColoredMana(Black)),
    "Zaphod BeebleBrox",
    Set(Creature),
    Set(Supertype.Legendary),
    Set("Regent", "Adventurer"),
    "text",
    2,
    1,
    multiverseId = Some(4)
  )


  val marvin = Card(
    Seq(GenericMana(3)),
    "Marvin the Paranoid Android",
    Set(Artifact, Creature),
    Set(Supertype.Legendary),
    Set("Android"),
    "1t: Tap target Creature",
    0,
    100,
    multiverseId = Some(5)
  )


  val restaurant = Card(
    Seq(),
    "The Restaurant at the End of the Universe",
    Set(Land),
    Set(Supertype.Legendary),
    Set(),
    "t: Add 2 generic mana to your mana pool",
    0,
    0,
    multiverseId = Some(6)
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
    Set(Supertype.Legendary),
    Set(),
    """|(5): Flip a coin. Heads, you win the game.
       |Tails you lose the game""".stripMargin,
    0,
    0,
    multiverseId = Some(7)
  )


  val petunias = Card(
    cost = Seq(GenericMana(0)),
    name = "Bowl of Petunias",
    types = Set(Creature),
    supertypes = Set.empty,
    subtypes = Set.empty,
    text = "Bowl of Petunias is Green",
    power = 0,
    toughness = 1,
    multiverseId = Some(8)
  )

}
