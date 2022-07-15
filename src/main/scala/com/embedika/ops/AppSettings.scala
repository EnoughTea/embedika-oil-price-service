package com.embedika.ops

import java.net.URI

import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.Try

import com.typesafe.config.{Config, ConfigFactory}


final case class DataGovRuSettings(oilPageLink: URI, oilPageCsvLinkQuery: String)


object DataGovRuSettings:
  def apply(config: Config): Try[DataGovRuSettings] = Try {
    DataGovRuSettings(URI(config.getString("oil-page-link")), config.getString("oil-page-csv-link-query"))
  }


final case class AppSettings(
    config: Config,
    host: String,
    port: Int,
    dataGovRu: DataGovRuSettings
)


object AppSettings:
  def apply(config: Config): Try[AppSettings] = Try {
    val appConfig = config.getConfig("application")
    AppSettings(
      config,
      appConfig.getString("host"),
      appConfig.getInt("port"),
      DataGovRuSettings(appConfig.getConfig("data-gov-ru")).get
    )
  }


trait HasSettings:
  lazy val config: Config = ConfigFactory.load()
  lazy val triedSettings: Try[AppSettings] = AppSettings(config)
