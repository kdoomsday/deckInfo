
lazy val root = project.in(file("."))
  .aggregate(core, swingView)
  .settings(
    name := "deckinfo",
    inThisBuild(List(
                  organization := "com.ebarrientos",
                  version := "1.0",
                  scalaVersion := "2.12.10"
                )),
  )

// General compiler settings
lazy val compilerSettings = Seq(
    testFrameworks += new TestFramework("utest.runner.Framework"),

    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
)

// Core project with types, loaders, calc, etc
lazy val core = (project in file("core"))
  .settings(
    compilerSettings,
    libraryDependencies ++= deps,
    libraryDependencies ++= circeDeps
  )

// project with only the swing ui
lazy val swingView = (project in file("swingView"))
  .dependsOn(core)
  .settings(
    compilerSettings,
    libraryDependencies ++= swingDeps
  )

lazy val cliView = (project in file("cliView"))
  .dependsOn(core)
  .settings(
    compilerSettings
  )

val zioVersion = "1.0.0-RC18-2"
lazy val deps = Seq(
  "com.typesafe.slick"       %% "slick"                    % "2.1.0",
  "com.h2database"            % "h2"                       % "1.3.148",
  // "com.github.wookietreiber" %% "scala-chart"              % "latest.integration",
  "com.github.wookietreiber" %% "scala-chart"              % "0.5.1",
  "org.scala-lang.modules"   %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules"   %% "scala-xml"                % "1.1.0",
  "org.json4s"               %% "json4s-jackson"           % "3.5.3",
  "dev.zio"                  %% "zio"                      % zioVersion,
  "com.lihaoyi"              %% "requests"                 % "0.5.1",

  // Test dependencies
  "com.lihaoyi" %% "utest" % "0.6.3" % "test"
)

val scalaSwingVersion = "2.1.1"
lazy val swingDeps = Seq(
  "org.scala-lang.modules" %% "scala-swing" % scalaSwingVersion
)


val circeVersion = "0.9.1"
lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

enablePlugins(JavaAppPackaging)
