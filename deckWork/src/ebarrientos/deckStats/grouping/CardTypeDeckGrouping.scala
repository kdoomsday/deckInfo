package ebarrientos.deckStats.grouping

import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.DeckEntry
import ebarrientos.deckStats.basics.CardType

/**
 * DeckGrouping that works on a card type basis
 */
class CardTypeDeckGrouping() extends DeckGrouping {

  override def group(deck: Deck): Map[GroupName, Seq[DeckEntry]] =
    val (creatures, noncreature)       = deck.cards.partition(_.card.is(CardType.Creature))
    val (planeswalkers, nonPW)         = noncreature.partition(_.card.is(CardType.Planeswalker))
    val (artifacts, nonArtifact)       = nonPW.partition(_.card.is(CardType.Artifact))
    val (enchantments, nonEnchantment) = nonArtifact.partition(_.card.is(CardType.Enchantment))
    val (instants, nonInstant)         = nonEnchantment.partition(_.card.is(CardType.Instant))
    val (sorceries, nonSorcery)        = nonInstant.partition(_.card.is(CardType.Sorcery))
    val (lands, other)                 = nonSorcery.partition(_.card.is(CardType.Land))
    Map(
      CardType.Creature.toString()     -> creatures,
      CardType.Planeswalker.toString() -> planeswalkers,
      CardType.Artifact.toString()     -> artifacts,
      CardType.Enchantment.toString()  -> enchantments,
      CardType.Instant.toString()      -> instants,
      CardType.Sorcery.toString()      -> sorceries,
      CardType.Land.toString()         -> lands,
      "Other"                          -> other
    ).withDefaultValue(Seq.empty)
}

object CardTypeDeckGrouping {
  def apply(): CardTypeDeckGrouping = new CardTypeDeckGrouping()
}
