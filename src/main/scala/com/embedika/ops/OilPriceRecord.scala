package com.embedika.ops

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import scala.math.Ordering.Implicits.*
import scala.util.Try

import io.circe.*
import io.circe.generic.semiauto.*
import squants.market.*


/** Represents a single oil price record, consisting of average oil price in the given date range. */
final case class OilPriceRecord(dates: DateRange, price: Money) extends Ordered[OilPriceRecord] {

  /** Compares this record's range start to another record's range start, from earliest to latest. */
  override def compare(that: OilPriceRecord): Int = dates.compare(that.dates)

  override def toString: String = s"$price $dates"
}


/** Represents an inclusive date range, from [[start]] to [[end]]. */
final case class DateRange(start: LocalDate, end: LocalDate) extends Ordered[DateRange] {

  /** Clamps given date between [[start]] and [[end]] dates of this range, inclusively. */
  def clamp(date: LocalDate): LocalDate = if (date <= start) start else if (date >= end) end else date

  /** Compares this range start date to another range start date, from earliest to latest. */
  override def compare(that: DateRange): Int = start.compareTo(that.start)

  /** Returns true if given date is in the inclusive range from [[start]] to [[end]]. */
  def contains(targetDate: LocalDate): Boolean = targetDate >= start && targetDate <= end

  /** Total number of days in this range. */
  def daysCount: Long = ChronoUnit.DAYS.between(start, end) + 1

  override def toString: String = s"$start, $end"
}


object DateRange {

  /** Parses ISO local date text range "2007-12-03, 2008-01-25" into a date range, or
    * a single string such as "2007-12-03" into a date range of a single day.
    */
  def parse(rangeOrSingleDate: String): Try[DateRange] = Try {
    val parts = rangeOrSingleDate.split(',')
    val (startRepr, endRepr) =
      if (parts.isEmpty) (rangeOrSingleDate, rangeOrSingleDate)
      else if (parts.length == 1) (parts(0), parts(0))
      else (parts(0), parts(1))
    parse(startRepr, endRepr)
  }.flatten

  /** Parses ISO local date texts such as "2007-12-03" for start and end dates into a date range. */
  def parse(start: String, end: String): Try[DateRange] = Try {
    DateRange(LocalDate.parse(start), LocalDate.parse(end))
  }
}


/** Provides Circe JSON support for [[Money]] */
trait MoneyJsonFormat extends RubMoneyContext {
  implicit val encodeMoney: Encoder[Money] = Encoder.encodeString.contramap[Money](_.rounded(1).toString)
  implicit val decodeMoney: Decoder[Money] = Decoder.decodeString.emapTry(Money(_))
}


/** Provides Circe JSON support for [[DateRange]] */
trait DateRangeJsonFormat {
  implicit val encodeDateRange: Encoder[DateRange] = Encoder.encodeString.contramap[DateRange](_.toString)
  implicit val decodeDateRange: Decoder[DateRange] = Decoder.decodeString.emapTry(DateRange.parse)
}


/** Provides Circe JSON support for [[OilPriceRecord]] */
trait OilPriceRecordJsonFormat extends DateRangeJsonFormat with MoneyJsonFormat {
  implicit val oilPriceRecordDecoder: Decoder[OilPriceRecord] = deriveDecoder
  implicit val oilPriceRecordEncoder: Encoder[OilPriceRecord] = deriveEncoder
}
