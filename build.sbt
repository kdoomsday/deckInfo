name := "deckstatsSBT"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.h2database" % "h2" % "1.3.148",
  "com.github.wookietreiber" %% "scala-chart" % "latest.integration",
  "org.scala-lang.modules" % "scala-swing_2.11" % "2.0.0-M2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.json4s" %% "json4s-jackson" % "3.3.0.RC2"
)