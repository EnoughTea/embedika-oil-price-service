package com.embedika.ops

import java.io.InputStreamReader
import java.util.stream.Stream

import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

import de.siegmar.fastcsv.reader.{CsvReader, CsvRow}


/** Trait for something capable of converting an input stream of CSV with oil prices into a typed representation. */
trait OilPriceCsvParser:
  /** Parses an input stream of CSV with oil prices into a vector of [[OilPriceRecord]]. */
  def parseCsv(csvContents: InputStreamReader): Try[Vector[OilPriceRecord]] =
    buildCsvReader(csvContents) flatMap {
      Using(_) { csvReader =>
        val rowsOrFirstFailure = Try(getRows(csvReader) map { parseRow(_).get })
        rowsOrFirstFailure map { rows =>
          rows.iterator().asScala.toVector
        }
      }.flatten
    }

  /** Creates a default CSV reader using ';' as a separator. */
  protected def buildCsvReader(csvContents: InputStreamReader): Try[CsvReader] =
    Try(CsvReader.builder().fieldSeparator(';').build(csvContents))

  /** Creates a stream of CSV rows with oil price records. By default just skips the first row in the document. */
  protected def getRows(csvReader: CsvReader): Stream[CsvRow] = csvReader.stream().skip(1)

  /** Implement this to actually parse a CSV row into an oil price record. */
  protected def parseRow(csvRow: CsvRow): Try[OilPriceRecord]

/** Parses CSV with oil prices from data.gov.ru */
trait DataGovRuOilPriceCsvParser extends OilPriceCsvParser:
  override protected def parseRow(csvRow: CsvRow): Try[OilPriceRecord] = ???
