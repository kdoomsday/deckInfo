package ebarrientos.deckStats.view.show

import java.awt.Color
import java.util.ResourceBundle

import scala.language.implicitConversions
import scala.swing.Alignment
import scala.swing.BoxPanel
import scala.swing.Component
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.Orientation

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.data.category.DefaultCategoryDataset

import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.basics.Deck
import ebarrientos.deckStats.basics.Instant
import ebarrientos.deckStats.basics.Land
import ebarrientos.deckStats.basics.Sorcery
import ebarrientos.deckStats.math.Calc

import scalax.chart.api._

/** Representation of deck data in a nice, formatted, style */
class FormattedStats extends ShowStats {
  private[this] lazy val text = ResourceBundle.getBundle("locale/formattedStats/text")

  lazy val component = buildComponent()

  private[this] lazy val avgCostLabel = new Label("")
  private[this] lazy val avgNonlandCostLabel = new Label("")
  private[this] lazy val landCountLabel = new Label("")
  private[this] lazy val creatureCountLabel = new Label("")
  private[this] lazy val instSorcCountLabel = new Label("")
  private[this] lazy val otherCountLabel = new Label("")

  lazy val curveArea = new GridPanel(2, 1)

  private[this] val doubleFormat = "%.02f"


  def show(d: Deck) = {
    val begin = System.currentTimeMillis()

    avgCostLabel.text = doubleFormat format Calc.avgManaCost(d)
    avgNonlandCostLabel.text = doubleFormat format Calc.avgManaCost(d, !_.is(Land))

    landCountLabel.text = Calc.count(d, _.is(Land)).toString
    creatureCountLabel.text = Calc.count(d, c => c.is(Creature) && !c.is(Land)).toString
    instSorcCountLabel.text = Calc.count(d, c => c.is(Instant) || c.is(Sorcery)).toString
    otherCountLabel.text =
      Calc.count(d, c => !c.is(Land) && !c.is(Creature) && !c.is(Instant) && !c.is(Sorcery)).toString

    curveArea.contents.clear
    curveArea.contents += curvePanel(d)
    curveArea.contents += symbolPanel(d)
//    printManaCurve(d)
//    curveArea.append("%n%s%n".format("-" * 20))
//    printSymbols(d)
    println(s"Calculation took ${System.currentTimeMillis() - begin} ms.")

    component.revalidate()
  }


  // Arrange the component
  private[this] def buildComponent() = {
    // I will use grids for all of this. Later I may switch to a gridbag or something else

    //align(landCountLabel, creatureCountLabel, instSorcCountLabel, otherCountLabel)


    val averages = new GridPanel(2, 2)
    averages.contents += new Label(text.getString("avgCost.all.label"))
    averages.contents += avgCostLabel
    averages.contents += new Label(text.getString("avgCost.nonLands.label"))
    averages.contents += avgNonlandCostLabel

    val sep = new scala.swing.Separator(Orientation.Horizontal)

    val counts = new GridPanel(4, 2)
    counts.contents += new Label(text.getString("count.lands.label"))
    counts.contents += landCountLabel
    counts.contents += new Label(text.getString("count.creatures.label"))
    counts.contents += creatureCountLabel
    counts.contents += new Label(text.getString("count.instSoc.label"))
    counts.contents += instSorcCountLabel
    counts.contents += new Label(text.getString("count.other.label"))
    counts.contents += otherCountLabel

    val info = new BoxPanel(Orientation.Vertical)
    info.contents += averages
    info.contents += sep
    info.contents += counts

    val panel = new GridPanel(1, 2)
    panel.contents += info
    panel.contents += curveArea
    panel
  }


  private[this] def curvePanel(d: Deck) = {
    val encodings = Calc.manaCurve(d) //.filterNot{ case (_, y) => y == 0}
    val chart = XYBarChart(encodings.toXYSeriesCollection("Cards per cost"))
    chart.title = "Mana Curve"
    chart.antiAlias = true
    chart.toComponent
  }


  private[this] def symbolPanel(d: Deck) = {
    import scala.language.implicitConversions

    /** To present mana color symbols in the right color. */
    class ManaBarRenderer(map: Map[String, Int]) extends BarRenderer {
      def mana2Color(s: String): Color = s match {
        case "W" => Color.white
        case "U" => Color.blue
        case "B" => Color.black
        case "R" => Color.red
        case "G" => Color.green
        case "C" => Color.gray
        case  _  => Color.pink
      }

      val mappings = new Array[Color](map.keys.size)
      map.keys.zipWithIndex foreach {
        case (manaString, index) => mappings(index) = mana2Color(manaString)
      }

      override def getItemPaint(row: Int, column: Int) = {
        mappings(row)
      }
    }


    val mapSymbols = Calc.manaSymbols(d)
    val data = new DefaultCategoryDataset
    val category = "Amount"
    for (m <- mapSymbols) m match {
      case (mana, amount) => data.addValue(amount, mana, category)
    }

    val chart = BarChart(data)
    chart.title = "Mana Symbols"
    val renderer = new ManaBarRenderer(mapSymbols)
    renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator)
    renderer.setBaseItemLabelsVisible(true)
    chart.plot.setRenderer(renderer)
    chart.orientation = Orientation.Horizontal
    chart.antiAlias = true
    chart.toComponent
  }


  /** Make a horizontal panel with n components. */
  private[this] def hGrid(components: Component*) = {
    val panel = new GridPanel(1, components.size)
    for (comp <- components)
      panel.contents += comp
    panel
  }


  /** Helper to build a label with a specified alignment. */
  private[this] def label(text: String, alignment: Alignment.Value = Alignment.Right) = {
    val lab = new Label(text)
    lab.horizontalAlignment = alignment
    lab
  }



  /** Align labels right */
  private[this] def align(labels: Label*)(implicit alignment: Alignment.Value = Alignment.Right) =
    for (label <- labels)
      label.horizontalAlignment = alignment
}