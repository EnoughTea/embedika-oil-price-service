package com.embedika.ops

import java.io.{InputStream, InputStreamReader}
import java.net.URI

import scala.concurrent.Future

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.*
import net.ruippeixotog.scalascraper.dsl.DSL.Extract.*
import net.ruippeixotog.scalascraper.dsl.DSL.Parse.*
import net.ruippeixotog.scalascraper.model.*


/** Used for something capable of providing something containing oil prices in an unknown format. */
trait OilPriceSource:
  /** Fetches oil prices from a local source. */
  def local()(using ec: IoExecutionContext): Future[InputStreamReader]

  /** Fetches oil prices from a remote source. */
  def remote()(using ec: IoExecutionContext): Future[InputStreamReader]


/** Provides CSV stream readers for data.gov.ru oil prices. */
final class DataGovRuOilPriceSource(httpClient: HttpClient) extends OilPriceSource:
  /** Fetches oil prices from a JAR resource. Not a good idea for a real service, but good enough for our purposes. */
  override def local()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
  }

  override def remote()(using ec: IoExecutionContext): Future[InputStreamReader] =
    val futureDocBytes = httpClient.get(URI("https://data.gov.ru/opendata/7710349494-urals")) // TODO: link to config
    futureDocBytes flatMap { bytes =>
      val browser = JsoupBrowser()
      val doc     = browser.parseInputStream(bytes)
      val maybeUtf8CsvLink: Option[String] =
        (doc >?> element("div.download:nth-child(3) > a:nth-child(1)")) map (_.attr("href"))
      maybeUtf8CsvLink map { csvLink =>
        httpClient.get(URI.create(csvLink)) map (new InputStreamReader(_))
      } getOrElse Future.failed(
        new RuntimeException("Cannot find download link for the data.gov.ru oil prices CSV (utf8)")
      )
    }
