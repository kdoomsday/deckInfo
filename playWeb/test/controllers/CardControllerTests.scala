package controllers

import utest._
import scala.io.Source
import play.api.test._
import play.api.mvc.MultipartFormData
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc.Result
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import play.api.test.Helpers._
import play.api.libs.Files
import java.io.File
import java.nio.file.Paths

object CardControllerTests extends TestSuite {
  implicit val actorSystem = ActorSystem()

  implicit val materializer =
    ActorMaterializer() // TODO No se como usar el system wide yet
  implicit val executor = scala.concurrent.ExecutionContext.global

  val duration = Duration.create(1, TimeUnit.SECONDS)

  val tests = Tests {

    test("deckUpload") {
      val path     = "/home/doomsday/code/deckInfo/playWeb/test/Test.dec"
      val dataFile = Paths.get(path)
      val tempFile = Files.SingletonTemporaryFileCreator.create(dataFile)
      val part     = MultipartFormData.FilePart(
        "deck",
        "Test",
        Some("application/octet-stream"),
        tempFile
      )
      val formData = MultipartFormData(dataParts = Map(), files=Seq(part), badParts = Seq())

      val request = FakeRequest(method = "POST", path = "/deck")
        .withMultipartFormDataBody(formData)
        .withHeaders("Content-Type" -> "undefined")

      val controller = new CardController(
        Helpers.stubControllerComponents(),
        new ZioRunnerDefault()
      )
      val result     = controller.deckStats.apply(request)
      val text       = contentAsString(result.run())

      assert(text == "texto")
    }
  }
}
