name := "stgy_client"

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "me.chrons" % "boopickle_sjs0.6_2.11" % "1.2.5"
)