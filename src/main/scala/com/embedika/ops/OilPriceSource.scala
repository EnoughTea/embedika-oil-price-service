package com.embedika.ops

import java.io.{InputStream, InputStreamReader}

import scala.concurrent.Future


/** Used for something capable of providing something containing oil prices in an unknown format. */
trait OilPriceSource:
  /** Fetches oil prices from a local source. */
  def local()(using ec: IoExecutionContext): Future[InputStreamReader]

  /** Fetches oil prices from a remote source. */
  def remote()(using ec: IoExecutionContext): Future[InputStreamReader]


/** Provides CSV stream readers for data.gov.ru oil prices. */
final class DataGovRuOilPriceSource() extends OilPriceSource:
  /** Fetches oil prices from a JAR resource. Not a good idea for a real service, but good enough for our purposes. */
  override def local()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
  }

  override def remote()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    new InputStreamReader(InputStream.nullInputStream())
  }
