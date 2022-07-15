package com.embedika.ops

import java.io.InputStreamReader

import scala.util.{Try, Using}

import org.scalatest.*
import org.scalatest.time.*


final class DataGovRuOilPriceCsvParserTests extends UnitSpec with DataGovRuOilPriceCsvParser:
  "An DataGovRuOilPriceCsvParser" should "parse test CSV" in {
    val parsed = Using(testCsv) { csvReader =>
      parseCsv(csvReader)
    }.flatten

    parsed.success.value.length shouldEqual 111
  }

  def testCsv: InputStreamReader =
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
