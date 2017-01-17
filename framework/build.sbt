name := "framework"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.play" %% "play-json" % "2.5.9",
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "com.typesafe.play" %% "play-json" % "2.5.3",
  "org.scala-lang" % "scala-swing" % "2.11.0-M7",
  "io.kamon" %% "kamon-core" % "0.6.0",
  "org.slf4j" % "slf4j-api" % "1.7.22",
  "org.slf4j" % "slf4j-simple" % "1.7.22",
 // "de.choffmeister" % "kamon-influxdb_2.11" % "0.1.1"
  "io.kamon" % "kamon-influxdb_2.11" % "0.6.3",
  "io.kamon" % "kamon-akka_2.11" % "0.6.3",
  "io.kamon" % "kamon-scala_2.11" % "0.6.3",
  "io.kamon" % "kamon-akka-remote_2.11" % "0.6.3",

  "io.kamon" % "kamon-system-metrics_2.11" % "0.6.3",
  "io.spray" % "spray-http_2.11" % "1.3.4",
 "org.aspectj" % "aspectjweaver" % "1.8.10"

)

