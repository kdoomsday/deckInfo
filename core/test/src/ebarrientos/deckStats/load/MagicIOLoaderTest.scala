package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.basics.Creature
import ebarrientos.deckStats.TestHelper
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import zio.ZIO
import scala.concurrent.duration.FiniteDuration
import requests.Response
import geny.Bytes
import ebarrientos.deckStats.basics.Card

object MagicIOLoaderTest extends TestSuite {
  val log = LoggerFactory.getLogger(getClass())

  private val path = "core/test/resources/MagicIOResponse.json"

  // Sample json response from MagicIO
  private val loadJson: zio.Task[String] =
    ZIO.scoped(
      ZIO.fromAutoCloseable(
        ZIO.attempt(scala.io.Source.fromFile(Paths.get(path).toFile()))
      )
        .map(src => src.getLines().mkString)
    )

  private val getCardResponse: zio.Task[Response] =
    loadJson.map { responseStr =>
      Response(url = "dummyURL",
               statusCode = 200,
               statusMessage = "OK",
               data = new Bytes(responseStr.getBytes()),
               headers = Map.empty,
               history = None)
    }

  private def cardChecks(card: Card) = {
    assert(card.power == 2)
    assert(card.toughness == 1)
    assert(card.text.startsWith("At the beginning"))
    assert(card.types.contains(Creature))
    assert(card.subtypes contains "Human")
    assert(card.subtypes contains "Wizard")
  }


  val tests = Tests {
    "load a card from its json" - {
      val loader = new MagicIOLoader(
        timeout = FiniteDuration(100, scala.concurrent.duration.SECONDS),
        retryTime = FiniteDuration(1, scala.concurrent.duration.SECONDS)
      )
      val res =
        loadJson.flatMap(jsonStr => loader.cardFromJsonString("Dark Confidant", jsonStr))
          .map {
            case Some(card) =>
              cardChecks(card)

            case None => throw new Exception("Did not parse card")
          }
      TestHelper.run(res)
    }

    "retry when the call fails" - {
      var requested = false
      var calls = 0
      val requester: MagicIOLoader.RequestParams => Response = _ => {
        calls += 1
        if (!requested) {
          requested = true
          Response(url = "dummyURL",
                   statusCode = 400,
                   statusMessage = "BadRequest",
                   data = new Bytes(Array.emptyByteArray),
                   headers = Map.empty,
                   history = None)
        }
        else {
          TestHelper.run(getCardResponse)
        }
      }

      val loader = new MagicIOLoader(
        timeout = FiniteDuration(100, scala.concurrent.duration.SECONDS),
        retryTime = FiniteDuration(50, scala.concurrent.duration.MILLISECONDS),
        requester
      )

      val res = loader.card("Dark Confidant").map {
        case Some(card) =>
          cardChecks(card)
          assert(calls == 2)

        case None => throw new Exception(s"Failed to get card. Requested=$requested - Calls=$calls")
      }

      TestHelper.run(res)
    }
  }
}
