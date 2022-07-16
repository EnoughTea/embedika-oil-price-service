import Dependencies._

ThisBuild / scalacOptions := Seq("-unchecked", "-deprecation", "-Xsource:3")
ThisBuild / scalaVersion  := "2.13.8"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name         := "oil-price-service",
    version      := "1.0.0-SNAPSHOT",
    organization := "com.embedika",
    libraryDependencies ++= opsDeps,
    Revolver.settings
  )
