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
  val zioHttpVersion      = "3.0.0-RC1"
  val circeVersion        = "0.13.0"

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

  val logbackDeps = Agg(
    ivy"ch.qos.logback:logback-classic:$logbackVersion"
  )

  object test extends ScalaTests with TestModule.Utest {
    def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:$utestVersion",
      ivy"org.mockito::mockito-scala:$mockitoScalaVersion"
    )
  }

  def scalacOptions = Seq("-deprecation", "-Xfatal-warnings")
}

object core extends MyModule {
  def ivyDeps = deps ++ circeDeps ++ logbackDeps

  def rootPath = T {
    os.pwd
  }
}

object zioWeb extends MyModule {
  val zioWebDeps = Agg(
    ivy"dev.zio::zio-http:$zioHttpVersion"
  )
  def ivyDeps = deps ++ circeDeps ++ logbackDeps ++ zioWebDeps

  def moduleDeps = Seq(core)

  def dockerBuild = T {
    assembly()
    val dockerImageName = "deckinfo"
    os.proc("docker", "build", "-f", "Dockerfile", "-t", s"$dockerImageName", ".").call()
  }
}
