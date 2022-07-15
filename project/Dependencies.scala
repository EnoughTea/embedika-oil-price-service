import sbt.*


object Dependencies {
  val configVersion         = "1.4.2"
  val logbackClassicVersion = "1.2.11"
  val scalaLoggingVersion   = "3.9.5"
  val scalatestVersion      = "3.2.12"

  val config         = "com.typesafe"                   % "config"          % configVersion
  val logbackClassic = "ch.qos.logback"                 % "logback-classic" % logbackClassicVersion
  val scalaLogging   = "com.typesafe.scala-logging"    %% "scala-logging"   % scalaLoggingVersion
  val scalatest      = "org.scalatest"                 %% "scalatest"       % scalatestVersion

  val loggingLibs = Seq(logbackClassic, scalaLogging)

  val testLibs = Seq(scalatest % Test)

  val opsDeps = Seq(
    config,
  ) ++ loggingLibs ++ testLibs
}
