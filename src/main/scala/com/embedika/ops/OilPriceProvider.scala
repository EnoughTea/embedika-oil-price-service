package com.embedika.ops

import java.io.InputStreamReader

import scala.concurrent.Future
import scala.util.control.NonFatal

import com.typesafe.scalalogging.StrictLogging


/** Used for something capable of providing current oil prices via some fetching strategy. */
trait OilPriceProvider {

  /** Provider name, should be unique. */
  def id: String

  /** Price sources. */
  def sources: OilPriceSource

  /** Fetches current prices from the [[sources]], usually tries remote source first then falls back to local souce.
    *
    * Returned vector is ordered by date range start.
    */
  def fetchCurrent()(implicit blockingEc: IoExecutionContext): Future[Vector[OilPriceRecord]]

  override def toString: String = s"$id (oil price provider)"

  protected def fetchingStrategy()(implicit blockingEc: IoExecutionContext): Future[InputStreamReader] =
    sources.remote() recoverWith { case NonFatal(_) => sources.local() }
}


/** Provides current oil prices from Data.gov.ru or, failing that, from local JAR resource. */
final class DataGovRuOilPrices(val httpClient: HttpClient, val sources: OilPriceSource)
    extends OilPriceProvider
    with DataGovRuOilPriceCsvParser
    with StrictLogging {
  def id: String = DataGovRuOilPrices.id

  override def fetchCurrent()(implicit
      blockingEc: IoExecutionContext
  ): Future[Vector[OilPriceRecord]] = {
    logger.trace(s"Data.gov.ru oil price provider is fetching current prices")
    fetchingStrategy() flatMap { contents => Future.fromTry(parseCsv(contents) map (_ sortBy (_.dates.start))) }
  }
}


object DataGovRuOilPrices {
  val id: String = "Data.gov.ru"
}
