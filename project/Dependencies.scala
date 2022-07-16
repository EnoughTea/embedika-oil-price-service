import sbt._


object Dependencies {
  val akkaVersion           = "2.6.19"
  val akkaHttpVersion       = "10.2.9"
  val akkaHttpCirceVersion  = "1.39.2"
  val circeVersion          = "0.14.2"
  val configVersion         = "1.4.2"
  val csvParserVersion      = "2.1.0"
  val logbackClassicVersion = "1.2.11"
  val scaffeineVersion      = "5.2.0"
  val scalaLoggingVersion   = "3.9.5"
  val scalaScraperVersion   = "3.0.0"
  val scalatestVersion      = "3.2.12"
  val squantsVersion        = "1.8.3"
  val sttpVersion           = "3.6.2"

  val config         = "com.typesafe"                   % "config"          % configVersion
  val csvParser      = "de.siegmar"                     % "fastcsv"         % csvParserVersion
  val logbackClassic = "ch.qos.logback"                 % "logback-classic" % logbackClassicVersion
  val scaffeine      = "com.github.blemale"            %% "scaffeine"       % scaffeineVersion % Compile
  val scalaLogging   = "com.typesafe.scala-logging"    %% "scala-logging"   % scalaLoggingVersion
  val scalaScraper   = "net.ruippeixotog"              %% "scala-scraper"   % scalaScraperVersion
  val scalatest      = "org.scalatest"                 %% "scalatest"       % scalatestVersion
  val sttp           = "com.softwaremill.sttp.client3" %% "core"            % sttpVersion
  val squants        = "org.typelevel"                 %% "squants"         % squantsVersion

  val akkaLibs = Seq(
    "com.typesafe.akka" %% "akka-actor-typed"  % akkaVersion,
    "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
    "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-caching" % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe"   % akkaHttpCirceVersion,
    "com.typesafe.akka" %% "akka-testkit"      % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )

  val circeLibs = Seq(
    "io.circe" %% "circe-core"    % circeVersion,
    "io.circe" %% "circe-parser"  % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion
  )

  val loggingLibs = Seq(logbackClassic, scalaLogging)

  val testLibs = Seq(scalatest % Test)

  val opsDeps = Seq(
    config,
    csvParser,
    scaffeine,
    scalaScraper,
    squants,
    sttp
  ) ++ akkaLibs ++ circeLibs ++ loggingLibs ++ testLibs
}
