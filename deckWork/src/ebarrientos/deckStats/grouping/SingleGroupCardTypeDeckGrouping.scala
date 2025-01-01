package ebarrientos.deckStats.grouping

import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.basics.DeckEntry
import ebarrientos.deckStats.basics.CardType.*
import scala.collection.mutable.{Seq => MSeq}
import ebarrientos.deckStats.basics.CardType
import scala.meta.contrib.DocToken.GroupName

/**
 * DeckGrouping that works on a card type basis and ensures each card is in a
 * single group.
 */
class SingleGroupCardTypeDeckGrouping() extends DeckGrouping {

  /** Helper to go from card type to GroupName in fewer characters */
  private inline def s(ct: CardType): GroupName = ct.toString()

  override def group(deck: Deck): Map[GroupName, Seq[DeckEntry]] =
    val initial = Map.empty[GroupName, MSeq[DeckEntry]].withDefaultValue(MSeq.empty[DeckEntry])
    deck
      .cards
      .foldLeft(initial) {
        case (acc, de) if de.card.is(Creature)     => acc + (s(Creature) -> acc(s(Creature)).appended(de))
        case (acc, de) if de.card.is(Planeswalker) => acc + (s(Planeswalker) -> acc(s(Planeswalker)).appended(de))
        case (acc, de) if de.card.is(Artifact)     => acc + (s(Artifact) -> acc(s(Artifact)).appended(de))
        case (acc, de) if de.card.is(Enchantment)  => acc + (s(Enchantment) -> acc(s(Enchantment)).appended(de))
        case (acc, de) if de.card.is(Instant)      => acc + (s(Instant) -> acc(s(Instant)).appended(de))
        case (acc, de) if de.card.is(Sorcery)      => acc + (s(Sorcery) -> acc(s(Sorcery)).appended(de))
        case (acc, de) if de.card.is(Land)         => acc + (s(Land)    -> acc(s(Land)).appended(de))
        case (acc, de)                             => acc + ("Other"    -> acc("Other").appended(de))
      }
      .map((k, mutableV) => (k, mutableV.toSeq))
      .withDefaultValue(Seq.empty) // Need this again because it gets lost after the map
}

object SingleGroupCardTypeDeckGrouping {
  def apply(): SingleGroupCardTypeDeckGrouping = new SingleGroupCardTypeDeckGrouping()
}
