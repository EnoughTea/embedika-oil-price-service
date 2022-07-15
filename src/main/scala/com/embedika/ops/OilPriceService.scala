package com.embedika.ops

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import scala.collection.Searching.{Found, InsertionPoint}
import scala.concurrent.Future
import scala.language.postfixOps

import squants.market.{Money, RUB}


/** Public contract for oil price-related operations. */
trait OilPriceServiceContract:
  /** Retrieves all oil price records from the provider with the given id.
    *
    * @param providerId Provider id.
    * @return All oil price records, or empty vector if no oil price records exist for given provider.
    */
  def allRecords(providerId: String): Future[Vector[OilPriceRecord]]

  /** Finds average oil price for the specified date range with the given oil price provider.
    *
    * @param targetRange Date range to calculate average price for,
    *                    should at least intersect dates in records.
    * @param providerId Provider id.
    * @return Calculated average oil price, or None of given range was completely outside the range of all oil records.
    */
  def priceInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]]

  /** Finds minimum and maximum average oil prices for the specified date with the given oil price provider.
    *
    * @param targetRange Date range to search for minimum and maximum prices,
    *                    should at least intersect dates in records.
    * @param providerId Provider id.
    * @return Found min and max prices, or None of given range was completely outside the range of all oil records.
    */
  def minMaxPricesInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[(Money, Money)]]

  /** Finds average oil price at the specified date with the given oil price provider.
    *
    * @param targetDate Date for an oil price record should be found.
    * @param providerId Provider id.
    * @return Found oil price or None.
    */
  def priceAtDate(targetDate: LocalDate, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]]


/** Contains business logic for various oil price-related operations.
  *
  * Assumes priceCache.get stores oil price records sorted by start date
  * due to [[OilPriceProvider.fetchCurrent]] guarantees.
  */
final class OilPriceService(priceCache: OilPriceCache) extends OilPriceServiceContract:
  override def allRecords(providerId: String): Future[Vector[OilPriceRecord]] = priceCache.get(providerId)

  override def priceInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]] = ???

  override def minMaxPricesInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[(Money, Money)]] = ???

  override def priceAtDate(targetDate: LocalDate, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]] = ???
