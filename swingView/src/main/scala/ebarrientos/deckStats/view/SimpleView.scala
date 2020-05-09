package ebarrientos.deckStats.view

import ebarrientos.deckStats.load._
import java.awt.Cursor
import java.awt.Cursor.{WAIT_CURSOR, getDefaultCursor}
import java.awt.Dimension
import java.util.ResourceBundle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing._
import scala.swing.BorderPanel
import scala.swing.BorderPanel.Position.{Center, East, West}
import scala.swing.event.ButtonClicked
import scala.util.{Failure, Success}
import ebarrientos.deckStats.view.show.{FormattedStats, ShowStats}
import ebarrientos.deckStats.basics.Deck
import javax.swing.UIManager

import scala.io.Source
import _root_.scala.util.control.Exception
// import zio.RTS

/** Main interface that shows a selector for the card database, a selector for the deck, and an
  * area for showing the deck stats.
  */
object SimpleView extends SimpleSwingApplication {
  lazy val text: ResourceBundle = ResourceBundle.getBundle("locale/text")
  lazy val pathDeck = new TextField
  lazy val pathCards = new TextField
  lazy val status = new Label(text.getString("statusbar.default"))

  lazy val prefSize = new Dimension(800, 400)

  private[this] var mainPanel: Panel = null


/*  lazy val netLoader = MagicIOLoader
  lazy val dbLoader = new H2DbLoader(xmlLoader)
  lazy val xmlLoader = new XMLCardLoader("""C:\Users\kdoom\Documents\code\deckInfo\src\main\resources\cards.xml""")
  lazy val cardLoader: CardLoader = new WeakCachedLoader(new SequenceLoader(dbLoader, /*xmlLoader,*/ netLoader))*/
  lazy val cardLoader: CardLoader =
    new MtgJsonLoader(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("AllCards.json")).mkString)
  private[this] var deckLoader: Option[DeckLoader] = None
  // What will actually show the information
  lazy val shower: ShowStats = new FormattedStats


  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)


  def top: MainFrame = new MainFrame {
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
  private[this] def actionOrError(block: => Unit): Unit = {
    try { block }
    catch {
      case e: Throwable => status.text = s"Error: ${e.getMessage()}"
    }
  }


  // When the deck is changed
  private[this] def changeDeck: Unit = {
    actionOrError {
      if (pathDeck.text != "") {
        deckLoader = Some(new XMLDeckLoader(pathDeck.text, cardLoader))
        calculate
      }
    }
  }


  /** Perform the action of loading the deck and showing the stats. */
  private[this] def calculate: Unit = {
    import java.awt.Cursor._
    import scala.concurrent.ExecutionContext.Implicits._

    val runtime = zio.Runtime.default

    // val task = Future {
    //   // Handle loading of cards database and such prop
    //   for (loader <- deckLoader) {
    //     status.text = text.getString("statusbar.loading")
    //     setCursor(WAIT_CURSOR)
    //     shower.show(Materializer.load(loader))
    //   }
    // }
    val task: Future[Deck] =
      // Handle loading of cards database and such prop
      deckLoader.fold(Future.failed[Deck](new Exception("No deck")))(dl => {
        status.text = text.getString("statusbar.loading")
        setCursor(WAIT_CURSOR)
        runtime.unsafeRunToFuture(dl.load())
      })


    task.onComplete {
      case Success(_) => Swing.onEDT {
        status.text = text.getString("statusbar.loaded")
        setCursor(getDefaultCursor())
      }
      case Failure(e) => Swing.onEDT {
        e.printStackTrace()
        status.text = text.getString("statusbar.error") + e.getMessage()
        setCursor(getDefaultCursor())
      }
    }

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
