name := "framework"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.11",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "com.typesafe.play" %% "play-json" % "2.5.9",
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "com.typesafe.play" %% "play-json" % "2.5.3"
)

