package ebarrientos.deckInfo.cli
import ebarrientos.deckStats.math.Calc
import ebarrientos.deckStats.load.XMLDeckLoader
import ebarrientos.deckStats.load.CardLoader
import ebarrientos.deckStats.load.{ DeckLoader, MtgJsonLoader }
import scala.io.Source
import ebarrientos.deckStats.basics._

import zio.{ App, Task }
import zio.console._
import zio.ZIO
import ebarrientos.deckStats.load.H2DBDoobieLoader
import ebarrientos.deckStats.load.MagicIOLoader

import pureconfig.generic.auto._
import pureconfig.ConfigSource
import ebarrientos.deckStats.config.CoreConfig
import scala.concurrent.ExecutionContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Entrypoint to the cli interface */
object RunMain extends App {

  final def run(args: List[String]) =
    logic(args).fold(_ => 1, _ => 0)

  def logic(args: List[String]) =
    for {
      path       <- path(args)
      _          <- putStrLn(s"Path = $path")
      config     <- ZIO.fromEither(ConfigSource.default.load[CoreConfig])
      _          <- putStrLn("Config loaded...")
      // cardLoader  = new MtgJsonLoader(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("AllCards.json")).mkString)
      ec          = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
      cardLoader  = new H2DBDoobieLoader(MagicIOLoader, config, ec)
      deckLoader  = new XMLDeckLoader(path, cardLoader)
      _          <- putStrLn("Loading deck...\n")
      deck       <- deckLoader.load()
      _          <- printDeck(deck)
    } yield ()

  def path(args: List[String]): Task[String] =
    Task {
      if (args.length == 0) {
        println("Usage: run <path/to/deck>")
        "/home/doomsday/Dropbox/decks/Experimental/Rock/The Rock .cod"
      }
      else {
        args(0)
      }
    }

  /** Print deck stats to the cli */
  private[this] def printDeck(d: Deck) = {
    for {
      _      <- putStr("")
      cards   = Calc.count(d)
      avgCost = Calc.avgManaCost(d)
      avgNonLands = Calc.avgManaCost(d, c => !c.is(Land))
      _ <- putStrLn(s"""|Cards: $cards
                        |Average cost: $avgCost
                        |Average (nonlands): $avgNonLands""".stripMargin)
      _ <- putStrLn("")

      creats = Calc.count(d, c => c.is(Creature))
      sorcs  = Calc.count(d, c => c.is(Sorcery))
      insts  = Calc.count(d, c => c.is(Instant))
      lands  = Calc.count(d, c => c.is(Land))
      arts   = Calc.count(d, c => c.is(Artifact))
      enchs  = Calc.count(d, c => c.is(Enchantment))
      walker = Calc.count(d, c => c.is(Planeswalker))
      permanents    = creats + arts + enchs + walker
      nonPermanents = sorcs + insts
      nonLands      = permanents + nonPermanents
      _ <- putStrLn(s"""|Permanents: $permanents
                        |Non premanents: $nonPermanents
                        |
                        |Creatures:     $creats
                        |Sorceries:     $sorcs
                        |Instants:      $insts
                        |Artifacts:     $arts
                        |Enchantments:  $enchs
                        |Planeswalkers: $walker
                        |------------------------
                        |               $nonLands""".stripMargin)
      _ <- putStrLn("")

      curve = Calc.manaCurve(d, c => true)
      _ <- printCurve(curve)
      _ <- putStrLn("")

      symbols = Calc.manaSymbols(d)
      _ <- printSymbols(symbols)
      _ <- putStrLn("")
    } yield ()
  }

  def printCurve(curve:  Seq[(Int, Int)]) =
    // Task.foreach(curve)((cost, amnt) => putStr(s"$cost: ").andThen(putStrLn('#'*amnt)))
    ZIO.foreach(curve){ case (cost, amnt) => putStrLn(s"$cost: ${"#"*amnt} ($amnt)") }

  def printSymbols(symbols: Map[String, Double]) =
    ZIO.foreach(symbols){ case (symbol, amnt) => putStrLn(s"$symbol: $amnt") }
}
