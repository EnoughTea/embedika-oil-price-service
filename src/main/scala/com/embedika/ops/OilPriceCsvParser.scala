package com.embedika.ops

import java.io.InputStreamReader
import java.text.{DecimalFormat, DecimalFormatSymbols}
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale
import java.util.stream.Stream

import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

import com.typesafe.scalalogging.StrictLogging
import de.siegmar.fastcsv.reader.{CsvReader, CsvRow}
import squants.market.{Money, MoneyContext, RUB}


/** Trait for something capable of converting an input stream of CSV with oil prices into a typed representation. */
trait OilPriceCsvParser extends StrictLogging {

  /** Parses an input stream of CSV with oil prices into a vector of [[OilPriceRecord]]. */
  def parseCsv(csvContents: InputStreamReader): Try[Vector[OilPriceRecord]] = {
    logger.trace("Parsing CSV from input stream")
    buildCsvReader(csvContents) flatMap {
      Using(_) { csvReader =>
        val rowsOrFirstFailure = Try(getRows(csvReader) map {
          parseRow(_).get
        })
        rowsOrFirstFailure map { rows =>
          val parsedRecords = rows.iterator().asScala.toVector
          logger.trace(s"Parsed CSV from input stream, total records: ${parsedRecords.length}")
          parsedRecords
        }
      }.flatten
    }
  }

  /** Creates a default CSV reader using ';' as a separator. */
  protected def buildCsvReader(csvContents: InputStreamReader): Try[CsvReader] =
    Try(CsvReader.builder().fieldSeparator(';').build(csvContents))

  /** Creates a stream of CSV rows with oil price records. By default just skips the first row in the document. */
  protected def getRows(csvReader: CsvReader): Stream[CsvRow] = csvReader.stream().skip(1)

  /** Implement this to actually parse a CSV row into an oil price record. */
  protected def parseRow(csvRow: CsvRow): Try[OilPriceRecord]
}


/** Parses CSV with oil prices from data.gov.ru */
trait DataGovRuOilPriceCsvParser extends OilPriceCsvParser with RubMoneyContext {
  private val monthValueToNameMap = Map[java.lang.Long, String](
    (1, "янв"),
    (2, "фев"),
    (3, "мар"),
    (4, "апр"),
    (5, "май"),
    (6, "июн"),
    (7, "июл"),
    (8, "авг"),
    (9, "сен"),
    (10, "окт"),
    (11, "ноя"),
    (12, "дек")
  ).asJava

  private val formatter = new DateTimeFormatterBuilder()
    .appendPattern("dd.")
    .appendText(ChronoField.MONTH_OF_YEAR, monthValueToNameMap)
    .appendPattern(".yy")
    .toFormatter

  private val decimalSymbols = new DecimalFormatSymbols(new Locale("ru"))
  private val decimalFormat  = new DecimalFormat("#,###.#", decimalSymbols)
  decimalFormat.setParseBigDecimal(true)

  override protected def parseRow(csvRow: CsvRow): Try[OilPriceRecord] = Try {
    val startDateRaw = csvRow.getField(0)
    val endDateRaw   = csvRow.getField(1)
    val priceRaw     = csvRow.getField(2)
    val startDate    = LocalDate.from(formatter.parse(startDateRaw))
    val endDate      = LocalDate.from(formatter.parse(endDateRaw))
    val price        = decimalFormat.parse(priceRaw).asInstanceOf[java.math.BigDecimal]
    OilPriceRecord(DateRange(startDate, endDate), Money(price))
  }
}


trait RubMoneyContext {
  implicit val mc: MoneyContext = MoneyContext(RUB, Set(RUB), Seq.empty, allowIndirectConversions = false)
}
