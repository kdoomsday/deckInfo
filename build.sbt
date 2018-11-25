name := "deckstatsSBT"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
                  organization := "com.ebarrientos",
                  version := "1.0",
                  scalaVersion := "2.12.6"
                )),

    libraryDependencies ++= deps,
    libraryDependencies ++= circeDeps,

    testFrameworks += new TestFramework("utest.runner.Framework"),

    scalacOptions += "-Ypartial-unification",
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
  )

val zioVersion     = "0.3.2"
lazy val deps = Seq(
  "com.typesafe.slick"       %% "slick"                    % "2.1.0",
  "com.h2database"            % "h2"                       % "1.3.148",
  "com.github.wookietreiber" %% "scala-chart"              % "latest.integration",
  "org.scala-lang.modules"   %% "scala-swing"              % "2.0.2",
  "org.scala-lang.modules"   %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules"   %% "scala-xml"                % "1.1.0",
  "org.json4s"               %% "json4s-jackson"           % "3.5.3",
  "org.scalaz"               %% "scalaz-zio"               % zioVersion,

  // Test dependencies
  "com.lihaoyi" %% "utest" % "0.6.3" % "test"
)


val circeVersion = "0.9.1"
lazy val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
