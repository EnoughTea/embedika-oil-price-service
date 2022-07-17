import Dependencies._
import com.typesafe.sbt.packager.docker._

ThisBuild / scalacOptions := Seq("-unchecked", "-deprecation", "-Xsource:3")
ThisBuild / scalaVersion  := "2.13.8"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, PackPlugin)
  .settings(
    name         := "oil-price-service",
    version      := "1.0.0",
    organization := "com.embedika",
    libraryDependencies ++= opsDeps,

    Universal / mappings += file("README.md") -> "README.md",
    maintainer := "Konstantin Zakharov <enough.tea@outlook.com>",
    packageSummary := name.value,
    packageDescription := """Oil price service for Embedika.""",

    dockerBaseImage := "openjdk:11-jre-slim",
    dockerExposedPorts := Seq(8045),
    dockerExposedVolumes := Seq("/var/log/embedika/oil-price-service"),

    Revolver.settings
  )
