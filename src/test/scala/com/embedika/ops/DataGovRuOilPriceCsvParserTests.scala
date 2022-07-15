package com.embedika.ops

import java.io.InputStreamReader
import java.time.LocalDate

import scala.util.{Try, Using}

import org.scalatest.*
import org.scalatest.time.*
import squants.market.{Money, MoneyContext, RUB}


final class DataGovRuOilPriceCsvParserTests extends UnitSpec with DataGovRuOilPriceCsvParser:
  "An DataGovRuOilPriceCsvParser" should "parse all records from the test CSV" in {
    val parsed = Using(testCsv) { csvReader =>
      parseCsv(csvReader)
    }.flatten

    parsed.success.value.length shouldEqual 111
  }

  it should "contain valid parsed records from the test CSV" in {
    val parsed = Using(testCsv) { csvReader =>
      parseCsv(csvReader)
    }.flatten

    val records = parsed.success.value
    records.head shouldEqual OilPriceRecord(
      DateRange(LocalDate.parse("2013-03-15"), LocalDate.parse("2013-04-14")), Money(764.6, RUB))
    records.last shouldEqual OilPriceRecord(
      DateRange(LocalDate.parse("2022-05-15"), LocalDate.parse("2022-06-14")), Money(638.7, RUB))
  }

  def testCsv: InputStreamReader =
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
