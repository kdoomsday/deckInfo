package ebarrientos.deckStats.view

import java.awt.Cursor
import java.awt.Cursor.WAIT_CURSOR
import java.awt.Cursor.getDefaultCursor
import java.awt.Dimension
import java.util.ResourceBundle
import scala.concurrent.ExecutionContext.Implicits.global
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.Center
import scala.swing.BorderPanel.Position.East
import scala.swing.BorderPanel.Position.West
import scala.swing.Button
import scala.swing.Component
import scala.swing.FileChooser
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.MainFrame
import scala.swing.Panel
import scala.swing.SimpleSwingApplication
import scala.swing.Swing
import scala.swing.TextField
import scala.swing.event.ButtonClicked
import ebarrientos.deckStats.load.DeckLoader
import ebarrientos.deckStats.load.XMLDeckLoader
import ebarrientos.deckStats.view.show.FormattedStats
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.view.show.ShowStats
import ebarrientos.deckStats.load.H2DbLoader
import scala.swing.FlowPanel
import ebarrientos.deckStats.load.WeakCachedLoader
import ebarrientos.deckStats.load.MagicIOLoader
import scala.concurrent.Future
import ebarrientos.deckStats.load.ScryCardLoader
import javax.swing.UIManager
import scala.util.{ Failure, Success }

/** Main interface that shows a selector for the card database, a selector for the deck, and an
  * area for showing the deck stats.
  */
object SimpleView extends SimpleSwingApplication {
  lazy val text = ResourceBundle.getBundle("locale/text")
  lazy val pathDeck = new TextField
  lazy val pathCards = new TextField
  lazy val status = new Label(text.getString("statusbar.default"))

  lazy val prefSize = new Dimension(800, 400)

  private[this] var mainPanel: Panel = null


  lazy val netLoader = MagicIOLoader
  lazy val dbLoader = new H2DbLoader(netLoader)
  lazy val cardLoader: CardLoader = new WeakCachedLoader(dbLoader)
  private[this] var deckLoader: Option[DeckLoader] = None
  // What will actually show the information
  lazy val shower: ShowStats = new FormattedStats


  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)


  def top = new MainFrame {
    title = text.getString("main.title")
    size = prefSize
    preferredSize = prefSize

    mainPanel = new BorderPanel {
      layout(selectorPanel(this)) = BorderPanel.Position.North
      layout(shower.component) = BorderPanel.Position.Center
      layout(status) = BorderPanel.Position.South
    }

    contents = mainPanel

    centerOnScreen()
  }

  // Full contents of the area that will be used for selecting deck file
  private[this] def selectorPanel(parent: Component) = {
    def mPanel(west: Component, center: Component, east: Component) = new BorderPanel {
      import BorderPanel.Position._
      layout(west) = West
      layout(center) = Center
      layout(east) = East
    }

    val labelDeck  = new Label(text.getString("deck.label"))
    //labelDeck.preferredSize = labelCards.preferredSize
    val buttonChooseDeck = new Button(text.getString("deck.buttonChoose"))
    val buttonReload = new Button(text.getString("deck.reload"))
    val bPanel = new FlowPanel
    bPanel.contents += buttonReload
    bPanel.contents += buttonChooseDeck

    val chooserDeck = new FileChooser


    val panel = new GridPanel(2, 1) {
      contents += mPanel(labelDeck, pathDeck, bPanel)

      listenTo(buttonChooseDeck)
      listenTo(buttonReload)
      reactions += {
        case ButtonClicked(`buttonChooseDeck`) =>
          if (chooserDeck.showOpenDialog(parent) == FileChooser.Result.Approve) {
            pathDeck.text = chooserDeck.selectedFile.getAbsolutePath()
            changeDeck
          }

        case ButtonClicked(`buttonReload`) =>
          changeDeck
      }
    }

    panel
  }


  /** Execute a block and, in case of error, show in the status bar. */
  private[this] def actionOrError(block: => Unit) = {
    try { block }
    catch {
      case e: Throwable => status.text = s"Error: ${e.getMessage()}"
    }
  }


  // When the deck is changed
  private[this] def changeDeck = {
    actionOrError {
      if (pathDeck.text != "") {
        deckLoader = Some(new XMLDeckLoader(pathDeck.text, cardLoader))
        calculate
      }
    }
  }


  /** Perform the action of loading the deck and showing the stats. */
  private[this] def calculate = {
    import java.awt.Cursor._
    import scala.concurrent.ExecutionContext.Implicits._

    val task = Future {
      // Handle loading of cards database and such prop
      for (loader <- deckLoader) {
        status.text = text.getString("statusbar.loading")
        setCursor(WAIT_CURSOR)
        shower.show(loader.load)
      }
    }

    task.onComplete { t => t match {
                       case Success(_) => Swing.onEDT {
                         status.text = text.getString("statusbar.loaded")
                         setCursor(getDefaultCursor())
                       }
                       case Failure(e) => Swing.onEDT {
                         e.printStackTrace()
                         status.text = text.getString("statusbar.error") + e.getMessage()
                         setCursor(getDefaultCursor())
                       }
    }}

    // task onSuccess {
    //   case _ => Swing.onEDT {
    //     status.text = text.getString("statusbar.loaded")
    //     setCursor(getDefaultCursor())
    //   }
    // }

    // task onFailure {
    //   case e: Throwable => Swing.onEDT {
    //     e.printStackTrace()
    //     status.text = text.getString("statusbar.error") + e.getMessage()
    //     setCursor(getDefaultCursor())
    //   }
    // }
  }


  // Cursor manipulation functions
  private[this] def setCursor(c: Cursor): Unit = mainPanel.cursor = c
  private[this] def setCursor(cType: Int): Unit = setCursor(Cursor.getPredefinedCursor(cType))
}
