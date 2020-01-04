package ebarrientos.deckStats.view.show

import scala.swing.Component
import ebarrientos.deckStats.basics.Deck

/** Given a deck, produce a representation of relevant information about it. */
trait ShowStats {

  /** Component this will use to show elements. */
  def component: Component

  /** Given a deck, show info in the managed component. */
  def show(d: Deck): Unit
}