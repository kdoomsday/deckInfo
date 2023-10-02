import mill._, scalalib._

trait MyModule extends ScalaModule {
  def scalaVersion = "2.13.11"

  val zioVersion          = "2.0.2"
  val zioCatsVersion      = "3.3.0"
  val utestVersion        = "0.7.2"
  val mockitoScalaVersion = "1.16.3"
  val quillVersion        = "4.6.0"
  val logbackVersion      = "1.4.5"
  val scalaXmlVersion     = "2.1.0"
  val zioHttpVersion      = "3.0.0-RC2"
  val circeVersion        = "0.13.0"
  val tapirVersion        = "1.7.5"

  val deps = Agg(
    ivy"javax.inject:javax.inject:1",
    ivy"com.google.inject:guice:7.0.0",
    ivy"com.h2database:h2:1.3.148",
    ivy"org.scala-lang.modules::scala-parser-combinators:2.2.0",
    ivy"org.scala-lang.modules::scala-xml:$scalaXmlVersion",
    ivy"org.json4s::json4s-native:3.7.0-M4",
    ivy"dev.zio::zio:$zioVersion",
    ivy"dev.zio::zio-interop-cats:$zioCatsVersion",
    ivy"com.lihaoyi::requests:0.5.1",
    ivy"com.github.pureconfig::pureconfig:0.12.3",
    ivy"io.getquill::quill-jdbc:$quillVersion",
    ivy"io.getquill::quill-jdbc-zio:$quillVersion"
  )

  val circeDeps = Agg(
    ivy"io.circe::circe-core:$circeVersion",
    ivy"io.circe::circe-generic:$circeVersion",
    ivy"io.circe::circe-parser:$circeVersion"
  )

  val logbackDeps = Agg(ivy"ch.qos.logback:logback-classic:$logbackVersion")

  object test extends ScalaTests with TestModule.Utest {

    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:$utestVersion",
      ivy"org.mockito::mockito-scala:$mockitoScalaVersion"
    )
  }

  def scalacOptions = Seq("-deprecation", "-Xfatal-warnings", "-Wunused")
}

/**
 * Core models and functionality. This will have the base Card and Deck models, plus calculations.
 * Also included are mechanisms to find cards, whether it be by going to a file, going to a
 * database, or to a web service
 */
object core extends MyModule {
  def ivyDeps = deps ++ circeDeps ++ logbackDeps

  def rootPath = T {
    os.pwd
  }
}

/** ZIO Https implementations */
object zioWeb extends MyModule {

  val zioWebDeps = Agg(
    ivy"dev.zio::zio-http:$zioHttpVersion"
  )

  def ivyDeps = deps ++ circeDeps ++ logbackDeps ++ zioWebDeps

  def moduleDeps = Seq(core)
}

/**
 * Tapir Endpoints
 */
object tapir extends MyModule {

  val tapirDeps = Agg(
    ivy"com.softwaremill.sttp.tapir::tapir-zio-http-server:$tapirVersion",
    ivy"com.softwaremill.sttp.tapir::tapir-prometheus-metrics:$tapirVersion",
    ivy"com.softwaremill.sttp.tapir::tapir-swagger-ui-bundle:$tapirVersion",
    ivy"com.softwaremill.sttp.tapir::tapir-json-zio:$tapirVersion"
  )

  def ivyDeps   = deps ++ circeDeps ++ logbackDeps ++ tapirDeps
  def moduleDeps = Seq(zioWeb)
}

/** Update the millw script. */
def millw() = T.command {
  val target = mill.util.Util.download("https://raw.githubusercontent.com/lefou/millw/main/millw")
  val millw  = build.millSourcePath / "mill"
  os.copy.over(target.path, millw)
  os.perms.set(millw, os.perms(millw) + java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE)
  target
}

/** Generate a Docker image from the assembly */
def dockerBuild = T {
  zioWeb.assembly()
  val dockerImageName = "deckinfo"
  os.proc("docker", "build", "-f", "Dockerfile", "-t", s"$dockerImageName", ".").call()
}
