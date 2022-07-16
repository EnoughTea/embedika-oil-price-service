package com.embedika.ops

import java.io.InputStreamReader
import java.net.URI

import scala.concurrent.Future
import scala.io.Codec

import com.typesafe.scalalogging.StrictLogging
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*


/** Used for something capable of providing something containing oil prices in an unknown format. */
trait OilPriceSource {

  /** Fetches oil prices from a local source. */
  def local()(implicit ec: IoExecutionContext): Future[InputStreamReader]

  /** Fetches oil prices from a remote source. */
  def remote()(implicit ec: IoExecutionContext): Future[InputStreamReader]
}


/** Provides stream readers with CSV document containing data.gov.ru oil prices. */
final class DataGovRuOilPriceSource(appSettings: AppSettings, httpClient: HttpClient)
    extends OilPriceSource
    with StrictLogging {

  /** Fetches oil prices from a JAR resource. Not a good idea for a real service, but good enough for our purposes. */
  override def local()(implicit ec: IoExecutionContext): Future[InputStreamReader] = Future {
    val localResource = "data-20220617T1317-structure-20210419T0745.csv"
    logger.trace(s"Data.gov.ru price source is sourcing local oil prices from $localResource")
    scala.io.Source.fromResource(localResource)(Codec.UTF8).reader()
  }

  override def remote()(implicit ec: IoExecutionContext): Future[InputStreamReader] = {
    logger.trace(s"Data.gov.ru price source is sourcing remote oil prices from ${appSettings.dataGovRu.oilPageLink}")
    val futureDocBytes = httpClient.get(appSettings.dataGovRu.oilPageLink)
    futureDocBytes flatMap { bytes =>
      val browser = JsoupBrowser()
      val doc     = browser.parseInputStream(bytes)
      val maybeUtf8CsvLink: Option[String] =
        (doc >?> element(appSettings.dataGovRu.oilPageCsvLinkQuery)) map (_.attr("href"))
      maybeUtf8CsvLink map { csvLink =>
        httpClient.get(URI.create(csvLink)) map (new InputStreamReader(_))
      } getOrElse Future.failed(
        new RuntimeException("Cannot find download link for the data.gov.ru oil prices CSV (utf8)")
      )
    }
  }
}
