name := "proto_framework"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val AkkaHttpVersion   = "2.4.7"
  Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % "2.4.2-RC3",
    "org.scalatest"     %% "scalatest"                 % "2.2.6"

  )
}

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.5.3"
    