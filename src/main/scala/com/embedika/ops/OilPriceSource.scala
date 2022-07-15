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
  override def local()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    new InputStreamReader(InputStream.nullInputStream())
  }

  override def remote()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    new InputStreamReader(InputStream.nullInputStream())
  }
