name := "deckstatsSBT"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.slick"       %% "slick"                    % "2.1.0",
  "com.h2database"            % "h2"                       % "1.3.148",
  "com.github.wookietreiber" %% "scala-chart"              % "latest.integration",
  "org.scala-lang.modules"   %% "scala-swing"              % "2.0.2",
  "org.scala-lang.modules"   %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules"   %% "scala-xml"                % "1.1.0",
  "org.json4s"               %% "json4s-jackson"           % "3.5.3",

  // Test dependencies
  "com.lihaoyi" %% "utest" % "0.6.3" % "test"
)


val circeVersion = "0.9.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

testFrameworks += new TestFramework("utest.runner.Framework")
