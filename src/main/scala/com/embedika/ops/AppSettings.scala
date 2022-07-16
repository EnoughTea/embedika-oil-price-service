package com.embedika.ops

import java.net.URI

import scala.language.postfixOps
import scala.util.Try

import com.typesafe.config.{Config, ConfigFactory}


final case class DataGovRuSettings(oilPageLink: URI, oilPageCsvLinkQuery: String)


final case class AppSettings(
    config: Config,
    host: String,
    port: Int,
    requestIdHeaderName: String,
    dataGovRu: DataGovRuSettings
)


object DataGovRuSettings {
  def apply(config: Config): Try[DataGovRuSettings] = Try {
    DataGovRuSettings(new URI(config.getString("oil-page-link")), config.getString("oil-page-csv-link-query"))
  }
}


object AppSettings {
  def apply(config: Config): Try[AppSettings] = Try {
    val appConfig = config.getConfig("application")
    AppSettings(
      config,
      appConfig.getString("host"),
      appConfig.getInt("port"),
      appConfig.getString("request-id-header-name"),
      DataGovRuSettings(appConfig.getConfig("data-gov-ru")).get
    )
  }
}
