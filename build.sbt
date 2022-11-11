/************
 * PROJECTS *
 ************/
lazy val root = project
  .in(file("."))
  .aggregate(core, cliView, web, playWeb)
  .settings(
    name := "deckinfo",
    inThisBuild(
      List(
        organization := "com.ebarrientos",
        version := "1.0",
        scalaVersion := "2.13.5"
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
    libraryDependencies  += guice
  )

lazy val cliView = (project in file("cliView"))
  .dependsOn(core)
  .settings(
    compilerSettings
  )

lazy val web = (project in file("web"))
  .enablePlugins(SbtTwirl)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= http4sDeps
  )

lazy val playWeb = (project in file("playWeb"))
  .dependsOn(core)
  .enablePlugins(PlayScala)
  .settings(
    compilerSettings,
    libraryDependencies ++= Seq(
      guice,
      "com.dripower" %% "play-circe" % "2812.0"
    ),
    libraryDependencies ++= testDeps
  )


/****************
 * DEPENDENCIES *
 ****************/
val zioVersion      = "2.0.2"
val zioCatsVersion  = "3.3.0"
val doobieVersion   = "0.8.8"
val utestVersion    = "0.7.2"
val quillVersion    = "4.6.0"

lazy val deps = Seq(
  "com.typesafe.slick"     %% "slick"                    % "3.3.2",
  "com.h2database"          % "h2"                       % "1.3.148",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.scala-lang.modules" %% "scala-xml"                % "2.0.0-M1",
  // "org.json4s"             %% "json4s-jackson"           % "3.7.0-M4",
  "org.json4s"             %% "json4s-native"            % "3.7.0-M4",
  "dev.zio"                %% "zio"                      % zioVersion,
  "dev.zio"                %% "zio-interop-cats"         % zioCatsVersion,
  "com.lihaoyi"            %% "requests"                 % "0.5.1",
  "com.github.pureconfig"  %% "pureconfig"               % "0.12.3",
  "org.tpolecat"           %% "doobie-core"              % doobieVersion,
  "org.tpolecat"           %% "doobie-h2"                % doobieVersion,
  "io.getquill"            %% "quill-jdbc"               % quillVersion,
  "io.getquill"            %% "quill-jdbc-zio"           % quillVersion

)

lazy val testDeps = Seq(
  // Test dependencies
  "com.lihaoyi" %% "utest" % utestVersion % "test"
)

val circeVersion = "0.13.0"

lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val http4sVersion   = "0.21.15"

lazy val http4sDeps = Seq(
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-twirl"
).map(_ % http4sVersion)

enablePlugins(JavaAppPackaging)
