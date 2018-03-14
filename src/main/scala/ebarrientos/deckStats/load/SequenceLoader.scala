package ebarrientos.deckStats.load

import ebarrientos.deckStats.basics.Card

/** Loader utilitario para poder intentar varios otros loaders secuencialmente */
class SequenceLoader(loaders: List[CardLoader]) extends CardLoader {
  def this(ls: CardLoader*) = this(ls.toList)

  def card(name: String): Option[Card] = {
    def loop(ls: List[CardLoader]): Option[Card] = ls match {
      case Nil => None
      case loader::rest => loader.card(name) match {
        case c @ Some(_) => c
        case None => loop(rest)
      }
    }
    loop(loaders)
  }
}
