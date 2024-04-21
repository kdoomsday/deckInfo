package ebarrientos.deckStats.load

import utest._
import ebarrientos.deckStats.TestHelper
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import zio.ZIO
import scala.concurrent.duration.FiniteDuration
import requests.Response
import geny.Bytes
import ebarrientos.deckStats.basics.Card
import ebarrientos.deckStats.load.MagicIOLoader.RequestParams
import ebarrientos.deckStats.basics.CardType

object MagicIOLoaderTest extends TestSuite {
  val log = LoggerFactory.getLogger(getClass())

  private val rootPath = "core/test/resources"
  private def responsePath(relative: String): String = s"$rootPath/$relative"
  private val path = responsePath("MagicIOResponse.json")

  private val defaultMaxRetries = 3

  /** Load a json as a string from a path */
  private def loadJson(pathString: String): zio.Task[String] =
    ZIO.scoped(
      ZIO.fromAutoCloseable(
        ZIO.attempt(scala.io.Source.fromFile(Paths.get(pathString).toFile()))
      )
        .map(src => src.getLines().mkString)
    )

  /** Load a json and create a sample response that includes the json as the body */
  private def getCardResponse(pathString: String): zio.Task[Response] =
    loadJson(pathString).map { responseStr =>
      Response(url = "dummyURL",
               statusCode = 200,
               statusMessage = "OK",
               data = new Bytes(responseStr.getBytes()),
               headers = Map.empty,
               history = None)
    }

  private def darkConfidantCardChecks(card: Card) = {
    assert(card.power == 2)
    assert(card.toughness == 1)
    assert(card.text.startsWith("At the beginning"))
    assert(card.types.contains(CardType.Creature))
    assert(card.subtypes contains "Human")
    assert(card.subtypes contains "Wizard")
    assert(card.multiverseId == Some(489754))
  }


  val tests = Tests {
    "load a card from its json" - {
      val loader = new MagicIOLoader(
        timeout = FiniteDuration(100, scala.concurrent.duration.SECONDS),
        retryTime = FiniteDuration(1, scala.concurrent.duration.SECONDS),
        maxRetries = defaultMaxRetries
      )
      val res =
        loadJson(path).flatMap(jsonStr => loader.cardFromJsonString("Dark Confidant", jsonStr))
          .map {
            case Some(card) =>
              darkConfidantCardChecks(card)

            case None => throw new Exception("Did not parse card")
          }
      TestHelper.run(res)
    }

    "retry when the call fails" - {
      retryTests(1) { (loader, requester) =>
        loader.card("Dark Confidant").map {
          case Some(card) =>
            darkConfidantCardChecks(card)
            assert(requester.failures == 1, requester.calls == 2)

          case None => throw new Exception(s"Failed to get card. Actual calls=${requester.calls}")
        }
      }
    }

    "retry only up to max retries" - {
      val retries = 3
      retryTests(100, maxRetries = retries) { (loader, requester) =>
        loader.card("Dark Confidant").map {
          case Some(card) =>
            throw new Exception("Should not have successfully found card")

          case None =>
            assert(requester.calls == retries + 1)
        }
      }
    }

    "load a two faced card" - {
      val loader = testLoader(TestRequester(loadPath = responsePath("MagicIO_InvasionOfZendikar_response.json")))
      val cardName = "Invasion of Zendikar"

      TestHelper.run { loader.card(cardName) }
        .fold(throw new Exception(s"Did not find card: $cardName")) { card =>
          assert(
            card.multiverseId == Some(607249),
            card.name == "Invasion of Zendikar",
            card.cmc == 4
          )
        }
    }
  }

  /**
   * Implements RequestParams => Response in a way that fails callsBeforeSuccess
   * times and then succeeds
   */
  private case class TestRequester(loadPath: String = path, failureCalls: Int = 0)
      extends Function[MagicIOLoader.RequestParams, Response] {

    var failures = 0
    var calls = 0

    override def apply(v1: RequestParams): Response = {
      calls += 1
      if (failures < failureCalls)
        failures += 1
        Response(url = "dummyURL",
                 statusCode = 400,
                 statusMessage = "BadRequest",
                 data = new Bytes(Array.emptyByteArray),
                 headers = Map.empty,
                 history = None)
      else
        TestHelper.run(getCardResponse(loadPath))
    }
  }

  /** Create a test MagicIOLoader */
  def testLoader(requester: RequestParams => Response, maxRetries: Int = defaultMaxRetries): MagicIOLoader =
    new MagicIOLoader(
      timeout = FiniteDuration(100, scala.concurrent.duration.SECONDS),
      retryTime = FiniteDuration(50, scala.concurrent.duration.MILLISECONDS),
      maxRetries = maxRetries,
      requester
    )

  private def retryTests(failureCalls: Int, maxRetries: Int = defaultMaxRetries)
                        (assertBlock: (MagicIOLoader, TestRequester) => ZIO[Any, Throwable, Unit]) = {
    val requester = new TestRequester(path, failureCalls)
    val loader = testLoader(requester, maxRetries)
    TestHelper.run(assertBlock(loader, requester))
  }
}
