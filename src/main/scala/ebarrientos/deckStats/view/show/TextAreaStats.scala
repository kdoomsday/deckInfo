package ebarrientos.deckStats.view.show

import ebarrientos.deckStats.basics.Deck

import scala.swing.{Component, TextArea}
import ebarrientos.deckStats.math.Calc
import ebarrientos.deckStats.math.Seqs.encode
import ebarrientos.deckStats.basics.Land
import ebarrientos.deckStats.basics.Creature
import java.awt.Font

/** Shows stats about a deck as a textual representation. */
class TextAreaStats extends ShowStats {
  private[this] val linesep = System.getProperty("line.separator")
  lazy val component = buildComponent

  /** Construct the component used for showing */
  private[this] def buildComponent: TextArea = {
    val area = new TextArea
    area.font = new Font(Font.MONOSPACED, Font.TRUETYPE_FONT, 12)
    area
  }


  def show(d: Deck): Unit = {
    component.text =
      s"""Avg mana cost: ${Calc.avgManaCost(d)}
      |Avg nonland cost: ${Calc.avgManaCost(d, !_.is(Land))}
      |-----
      |Lands: ${Calc.count(d, c => c.is(Land) && !c.is(Creature))}
      |Creatures: ${Calc.count(d, _.is(Creature))}
      |Other: ${Calc.count(d, c => !c.is(Creature) && !c.is(Land))}
      |-----
      |""".stripMargin

    printManaCurve(d)

    component.revalidate()
  }


  /** Print the mana curve into the text area. */
  private[this] def printManaCurve(d: Deck): Unit = {
    val encodings = Calc.manaCurve(d)

    for ((cost, amount) <- encodings) {
      component.append("%2d: [%2d] ".format(cost, amount))
      component.append("*" * amount)
      component.append(linesep)
    }
  }
}
