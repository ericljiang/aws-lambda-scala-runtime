val scala3Version = "2.13.6"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scala runtime for AWS Lambda",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,
  )
