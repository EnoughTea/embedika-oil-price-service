package com.embedika.ops

import java.io.InputStreamReader

import scala.concurrent.Future
import scala.util.control.NonFatal

import com.typesafe.scalalogging.StrictLogging


/** Used for something capable of providing current oil prices via some fetching strategy. */
trait OilPriceProvider extends StrictLogging {

  /** Provider name, should be unique and case-insensitive. */
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
    sources.remote() recoverWith { case NonFatal(e) =>
      logger.trace(s"Oil price provider is falling to local source due to remote being unavailable: ${e.getMessage}")
      sources.local()
    }
}


/** Provides current oil prices from Data.gov.ru or, failing that, from local JAR resource. */
final class DataGovRuOilPrices(val sources: OilPriceSource)
    extends OilPriceProvider
    with DataGovRuOilPriceCsvParser {
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
