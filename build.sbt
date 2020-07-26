
lazy val root = project.in(file("."))
  .aggregate(core, cliView)
  .settings(
    name := "deckinfo",
    inThisBuild(List(
                  organization := "com.ebarrientos",
                  version := "1.0",
                  scalaVersion := "2.13.2"
                )),
  )

// General compiler settings
lazy val compilerSettings = Seq(
    testFrameworks += new TestFramework("utest.runner.Framework"),
    // addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
)

// Core project with types, loaders, calc, etc
lazy val core = (project in file("core"))
  .settings(
    compilerSettings,
    libraryDependencies ++= deps,
    libraryDependencies ++= circeDeps
  )

lazy val cliView = (project in file("cliView"))
  .dependsOn(core)
  .settings(
    compilerSettings
  )

val zioVersion = "1.0.0-RC18-2"
val zioCatsVersion = "2.0.0.0-RC13"
val doobieVersion = "0.8.8"

lazy val deps = Seq(
  "com.typesafe.slick"       %% "slick"                    % "3.3.2",
  "com.h2database"            % "h2"                       % "1.3.148",
  "org.scala-lang.modules"   %% "scala-parser-combinators" % "1.1.2",
  "org.scala-lang.modules"   %% "scala-xml"                % "2.0.0-M1",
  "org.json4s"               %% "json4s-jackson"           % "3.7.0-M4",
  "dev.zio"                  %% "zio"                      % zioVersion,
  "dev.zio"                  %% "zio-interop-cats"         % zioCatsVersion,
  "com.lihaoyi"              %% "requests"                 % "0.5.1",
  "com.github.pureconfig"    %% "pureconfig"               % "0.12.3",

  "org.tpolecat"             %% "doobie-core"              % doobieVersion,
  "org.tpolecat"             %% "doobie-h2"                % doobieVersion,


  // Test dependencies
  "com.lihaoyi" %% "utest" % "0.7.2" % "test"
)

val circeVersion = "0.13.0"
lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

enablePlugins(JavaAppPackaging)
