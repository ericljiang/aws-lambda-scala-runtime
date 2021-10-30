val scala3Version = "2.13.6"

lazy val root = project
  .in(file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "Scala runtime for AWS Lambda",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "circe" % "3.3.16",
      "com.softwaremill.sttp.client3" %% "core" % "3.3.16",
      "com.softwaremill.sttp.client3" %% "httpclient-backend" % "3.3.16",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser" % "0.14.1",
    ),

    Compile / mainClass := Some("Main")
  )
