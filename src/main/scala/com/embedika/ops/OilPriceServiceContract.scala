package com.embedika.ops

import java.time.LocalDate

import scala.concurrent.Future

import squants.market.Money


/** Public contract for oil price-related operations. */
trait OilPriceServiceContract {

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
    * @param providerId  Provider id.
    * @return Calculated average oil price, or None of given range was completely outside the range of all oil records.
    */
  def priceInDateRange(targetRange: DateRange, providerId: String)(implicit
      ec: CpuExecutionContext
  ): Future[Option[Money]]

  /** Finds minimum and maximum average oil prices for the specified date with the given oil price provider.
    *
    * @param targetRange Date range to search for minimum and maximum prices,
    *                    should at least intersect dates in records.
    * @param providerId  Provider id.
    * @return Found min and max prices, or None of given range was completely outside the range of all oil records.
    */
  def minMaxPricesInDateRange(targetRange: DateRange, providerId: String)(implicit
      ec: CpuExecutionContext
  ): Future[Option[(Money, Money)]]

  /** Finds average oil price at the specified date with the given oil price provider.
    *
    * @param targetDate Date for an oil price record should be found.
    * @param providerId Provider id.
    * @return Found oil price or None.
    */
  def priceAtDate(targetDate: LocalDate, providerId: String)(implicit
      ec: CpuExecutionContext
  ): Future[Option[Money]]
}
