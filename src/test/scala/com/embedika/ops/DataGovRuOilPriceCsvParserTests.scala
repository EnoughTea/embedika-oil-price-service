package com.embedika.ops

import java.io.InputStreamReader

import scala.util.Using

import com.embedika.ops.utils.UnitSpec
import squants.market.{Money, RUB}


final class DataGovRuOilPriceCsvParserTests extends UnitSpec with DataGovRuOilPriceCsvParser {
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
      DateRange.parse("2013-03-15", "2013-04-14").get,
      Money(764.6, RUB)
    )
    records.last shouldEqual OilPriceRecord(
      DateRange.parse("2022-05-15", "2022-06-14").get,
      Money(638.7, RUB)
    )
  }

  def testCsv: InputStreamReader =
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
}
