ThisBuild / evictionErrorLevel := Level.Info
ThisBuild / maintainer := "Eduardo Barrientos"

/************
 * PROJECTS *
 ************/
lazy val root = project
  .in(file("."))
  .aggregate(core)
  .settings(
    name := "deckinfo",
    inThisBuild(
      List(
        organization := "com.ebarrientos",
        version := "1.0",
        scalaVersion := "2.13.10"
      )
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )

// General compiler settings
lazy val compilerSettings = Seq(
  testFrameworks += new TestFramework("utest.runner.Framework")
)

// Core project with types, loaders, calc, etc
lazy val core = (project in file("core"))
  .settings(
    compilerSettings,
    libraryDependencies ++= deps,
    libraryDependencies ++= circeDeps,
    libraryDependencies ++= testDeps,
    libraryDependencies  += guice,
    libraryDependencies ++= logbackDeps
  )

lazy val zioWeb = (project in file("zioWeb"))
  .dependsOn(core)
  .settings(
    compilerSettings,
    libraryDependencies ++= zioWebDeps
  )


/****************
 * DEPENDENCIES *
 ****************/
val zioVersion          = "2.0.2"
val zioCatsVersion      = "3.3.0"
val utestVersion        = "0.7.2"
val mockitoScalaVersion = "1.16.3"
val quillVersion        = "4.6.0"
val logbackVersion      = "1.4.5"
val scalaXmlVersion     = "2.1.0"
val zioHttpVersion      = "3.0.0-RC1"

lazy val deps = Seq(
  "com.typesafe.slick"     %% "slick"                    % "3.3.2",
  "com.h2database"          % "h2"                       % "1.3.148",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.2.0",
  "org.scala-lang.modules" %% "scala-xml"                % scalaXmlVersion,
  // "org.json4s"             %% "json4s-jackson"           % "3.7.0-M4",
  "org.json4s"             %% "json4s-native"            % "3.7.0-M4",
  "dev.zio"                %% "zio"                      % zioVersion,
  "dev.zio"                %% "zio-interop-cats"         % zioCatsVersion,
  "com.lihaoyi"            %% "requests"                 % "0.5.1",
  "com.github.pureconfig"  %% "pureconfig"               % "0.12.3",
  "io.getquill"            %% "quill-jdbc"               % quillVersion,
  "io.getquill"            %% "quill-jdbc-zio"           % quillVersion
)

lazy val testDeps = Seq(
  // Test dependencies
  "com.lihaoyi" %% "utest"         % utestVersion        % Test,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion % Test
)

val circeVersion = "0.13.0"

lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


lazy val logbackDeps = Seq(
  // "ch.qos.logback" % "logback-classic" % logbackVersion % Test
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

lazy val zioWebDeps = Seq(
  "dev.zio" %% "zio-http" % zioHttpVersion
)

enablePlugins(JavaAppPackaging)

ThisBuild / run / fork := true
