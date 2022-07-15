import sbt.*


object Dependencies {
  val configVersion         = "1.4.2"
  val csvParserVersion      = "2.1.0"
  val logbackClassicVersion = "1.2.11"
  val scalaLoggingVersion   = "3.9.5"
  val scalaScraperVersion   = "3.0.0"
  val scalatestVersion      = "3.2.12"
  val squantsVersion        = "1.8.3"
  val sttpVersion           = "3.6.2"

  val config         = "com.typesafe"                   % "config"          % configVersion
  val csvParser      = "de.siegmar"                     % "fastcsv"         % csvParserVersion
  val logbackClassic = "ch.qos.logback"                 % "logback-classic" % logbackClassicVersion
  val scalaLogging   = "com.typesafe.scala-logging"    %% "scala-logging"   % scalaLoggingVersion
  val scalaScraper   = "net.ruippeixotog"              %% "scala-scraper"   % scalaScraperVersion
  val scalatest      = "org.scalatest"                 %% "scalatest"       % scalatestVersion
  val sttp           = "com.softwaremill.sttp.client3" %% "core"            % sttpVersion
  val squants        = "org.typelevel"                 %% "squants"         % squantsVersion

  val loggingLibs = Seq(logbackClassic, scalaLogging)

  val testLibs = Seq(scalatest % Test)

  val opsDeps = Seq(
    config,
    csvParser,
    scalaScraper,
    squants,
    sttp
  ) ++ loggingLibs ++ testLibs
}
