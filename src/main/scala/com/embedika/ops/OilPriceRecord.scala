package com.embedika.ops

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import scala.util.Try

import squants.Money


/** Represents an inclusive date range, from [[start]] to [[end]]. */
final case class DateRange(start: LocalDate, end: LocalDate) extends Ordered[DateRange]:
  /** Compares this range start date to another range start date, from earliest to latest. */
  override def compare(that: DateRange): Int = start.compareTo(that.start)

  override def toString: String = s"[$start, $end]"


object DateRange:
  /** Parses ISO local date text such as "2007-12-03" into a date range of a single day. */
  def parse(singleDate: String): Try[DateRange] = Try {
    DateRange(LocalDate.parse(singleDate), LocalDate.parse(singleDate))
  }

  /** Parses ISO local date text such as "2007-12-03" for start and end dates into a date range. */
  def parse(start: String, end: String): Try[DateRange] = Try {
    DateRange(LocalDate.parse(start), LocalDate.parse(end))
  }


/** Represents a single oil price record, consisting of average oil price in the given date range. */
final case class OilPriceRecord(dateRange: DateRange, price: Money) extends Ordered[OilPriceRecord]:
  /** Compares this record's range start to another record's range start, from earliest to latest. */
  override def compare(that: OilPriceRecord): Int = dateRange.compare(that.dateRange)

  override def toString: String = s"$price $dateRange"
