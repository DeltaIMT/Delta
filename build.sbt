scalaVersion in ThisBuild := "2.11.8"

scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")

lazy val root = project in file(".") aggregate(framework,stgy_server,stgy_client, paint_server, championship_server)


lazy val framework = project in file("framework") settings(
  version := "1.0.0"
)

lazy val stgy_server = project in file("stgy_server") settings(
  version := "1.0.0"
) dependsOn(framework)

lazy val stgy_client = project in file("stgy_client") settings(
  version := "1.0.0"
) dependsOn(stgy_server)

lazy val paint_server = project in file("paint_server") settings(
  version := "1.0.0"
) dependsOn(framework)

lazy val championship_server = project in file("championship_server") settings(
  version := "1.0.0"
) dependsOn(framework)


