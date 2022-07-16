package com.embedika.ops

import java.time.LocalDate

import scala.collection.Searching.{Found, InsertionPoint}
import scala.concurrent.Future
import scala.language.postfixOps

import com.typesafe.scalalogging.StrictLogging
import squants.market.{Money, RUB}


/** Contains business logic for various oil price-related operations.
  *
  * Assumes priceCache.get stores oil price records sorted by start date
  * due to [[OilPriceProvider.fetchCurrent]] guarantees.
  */
final class OilPriceService(priceCache: OilPriceCache)
    extends OilPriceServiceContract
    with OilPriceRecordSearchHelper
    with StrictLogging:
  override def allRecords(providerId: String): Future[Vector[OilPriceRecord]] = priceCache.get(providerId)

  override def priceInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]] =
    logger.trace(s"Finding average price in range $targetRange for $providerId")
    findRecordsForDateRangeFromProvider(targetRange, providerId) map {
      case Vector()       => None
      case Vector(single) => Some(single.price)
      case relatedRecords @ Vector(first, rest*) =>
        val startDate = first.dates.clamp(targetRange.start)
        val endDate   = rest.last.dates.clamp(targetRange.end)
        // Calculate price periods: a price and amount of days said price is held:
        val pricePeriods = relatedRecords.zipWithIndex map { case (record, index) =>
          // Handle first and last periods separately, since they could contain any amount of days:
          val participatingDays =
            if index == 0 then DateRange(startDate, record.dates.end).daysCount
            else if index == relatedRecords.length - 1 then DateRange(record.dates.start, endDate).daysCount
            else record.dates.daysCount
          (record.price, participatingDays)
        }
        // Now we just need to calculate (<period price> * <days in period>) / <total days in all periods>
        val (sum, totalDays) = pricePeriods.foldLeft((Money(0, RUB), 0L)) {
          case (acc, (priceForPeriod, daysInPeriod)) =>
            (acc._1 + (priceForPeriod * daysInPeriod.toDouble), acc._2 + daysInPeriod)
        }
        Some(sum / totalDays.toDouble)
    } map { foundPrice =>
      logger.trace(s"Found average price in range $targetRange for $providerId: $foundPrice")
      foundPrice
    }

  /** Same as [[findRecordsForDateRange]], but fetches prices from price provider.
    *
    * @return Found oil price records and their indices, sorted by date range start,
    *         or an empty vector if targetRange does not intersect with any record.
    */
  private def findRecordsForDateRangeFromProvider(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Vector[OilPriceRecord]] =
    priceCache.get(providerId) map { findRecordsForDateRange(targetRange, _) }

  override def minMaxPricesInDateRange(targetRange: DateRange, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[(Money, Money)]] =
    logger.trace(s"Finding min&max prices in range $targetRange for $providerId")
    findRecordsForDateRangeFromProvider(targetRange, providerId) map {
      case Vector()       => None
      case Vector(single) => Some((single.price, single.price))
      case relatedRecords =>
        val initial = (relatedRecords.head.price, relatedRecords.head.price)
        val minMax = relatedRecords.foldLeft(initial) { case ((min, max), record) =>
          (if record.price < min then record.price else min, if record.price > max then record.price else max)
        }
        Some(minMax)
    } map { foundPrices =>
      logger.trace(s"Min&max prices in range $targetRange for $providerId: $foundPrices")
      foundPrices
    }

  override def priceAtDate(targetDate: LocalDate, providerId: String)(using
      ec: CpuExecutionContext
  ): Future[Option[Money]] =
    logger.trace(s"Finding price at a date $targetDate for $providerId")
    priceCache.get(providerId) map { prices =>
      findRecordForDate(targetDate, prices).map(_._1.price)
    } map { foundPrice =>
      logger.trace(s"Found price at a date $targetDate for $providerId: $foundPrice")
      foundPrice
    }


/** Contains a few helper methods to search for oil price records more conveniently. */
trait OilPriceRecordSearchHelper:
  private type OilPriceRecordWithIndex = (OilPriceRecord, Int)

  /** Finds oil price records whose date ranges intersects with given target range.
    *
    * @param targetRange Date range for which intersecting oil price records should be found.
    * @param prices Vector of oil price records, sorted by date range start.
    * @return Found oil price records and their indices, sorted by date range start,
    *         or an empty vector if targetRange does not intersect with any record.
    */
  protected def findRecordsForDateRange(
      targetRange: DateRange,
      prices: Vector[OilPriceRecord]
  ): Vector[OilPriceRecord] =
    ((findRecordForDate(targetRange.start, prices), findRecordForDate(targetRange.end, prices)) match {
      case (Some(start), Some(end)) => Some(start._2 to end._2)           // Given range is completely known
      case (Some(start), None)      => Some(start._2 until prices.length) // Given range goes too far into future...
      case (None, Some(end))        => Some(0 to end._2)                  // Given range goes to far into past...
      case _ => None // Given range is completely disjointed with known date ranges
    }) map { affectedRange => affectedRange map { i => prices(i) } toVector } getOrElse Vector.empty

  /** Finds oil price record whose date range includes given targetDate.
    *
    * @param targetDate Date for which oil price record should be found.
    * @param prices Vector of oil price records, sorted by date range start.
    * @return Found oil price record and its index, or None if no such record exists.
    */
  protected def findRecordForDate(
      targetDate: LocalDate,
      prices: Vector[OilPriceRecord]
  ): Option[OilPriceRecordWithIndex] =
    // search() is a binary search, so its result can tell us where the targetDate is relative to overall date range:
    prices.search(OilPriceRecord(DateRange(targetDate, targetDate), Money(0, RUB))) match {
      // Oil price record exists exactly for targetDate.
      case Found(foundIndex) => Some(prices(foundIndex) -> foundIndex)
      // targetDate is earlier than any record, so no record for us.
      case InsertionPoint(insertionPoint) if insertionPoint == 0 => None
      // targetDate either belongs to the last record's date range or is more into the future than any record.
      case InsertionPoint(insertionPoint) if insertionPoint == prices.length =>
        val lastRecord = prices(insertionPoint - 1)
        if lastRecord.dates.contains(targetDate) then Some((lastRecord, insertionPoint - 1)) else None
      // targetDate is in the date range of the record at insertionPoint - 1
      case InsertionPoint(insertionPoint) => Some((prices(insertionPoint - 1), insertionPoint - 1))
    }
