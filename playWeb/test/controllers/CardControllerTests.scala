package controllers

import utest._
import scala.io.Source
import play.api.test._
import play.api.mvc.MultipartFormData
import akka.util.ByteString
import play.api.libs.streams.Accumulator
import play.api.mvc.Result
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import play.api.test.Helpers._
import play.api.libs.Files
import java.io.File
import java.nio.file.Paths
import java.util.UUID
import play.api.Application
import play.api.Logger
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.PlayBodyParsers
import play.api.mvc.AnyContentAsMultipartFormData

object CardControllerTests extends TestSuite {
  val log = Logger(getClass())

  implicit val actorSystem = ActorSystem()

  val duration = Duration.create(1, TimeUnit.SECONDS)

  val tests = Tests {

    test("deckUpload") {
      val path     = "test/Test.dec"
      val dataFile = Paths.get(path)

      log.debug(s"Path: ${dataFile.getFileName()} - Size: ${java.nio.file.Files.size(dataFile)}")

      val tempFile = Files.SingletonTemporaryFileCreator.create(dataFile)
      val part     = MultipartFormData.FilePart(
        "deck",
        "Test",
        Some("application/xml"),
        tempFile
      )
      val formData = MultipartFormData(dataParts = Map(), files=Seq(part), badParts = Seq())
      val anyc = AnyContentAsMultipartFormData(formData)

      // val request = FakeRequest(method = "POST", path = "/deck")
      //   .withHeaders("Content-Type" -> ("multipart/form-data; boundary=--dummy"))
      //   .withMultipartFormDataBody(formData)
      val request = FakeRequest(method = "POST", path = "/deck")
        .withBody(anyc)
        .withHeaders("Content-Type" -> ("multipart/form-data; boundary=--dummy"))

      val controller = new CardController(
        Helpers.stubControllerComponents(playBodyParsers = PlayBodyParsers()),
        new ZioRunnerDefault()
      )
      val result     = controller.deckStats.apply(request)
      val text       = contentAsString(result.run())

      assert(text == "texto")
    }
  }
}
