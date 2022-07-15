package com.embedika.ops

class DataGovRuOilPricesTests extends UnitSpec:
  "A DataGovRuOilPrices" should "fetch prices from test sources" in {
    val provider = makePriceProvider()

    whenReady(provider.fetchCurrent()) { prices =>
      prices.length shouldEqual 111
    }
  }

  def makePriceProvider(): DataGovRuOilPrices =
    DataGovRuOilPrices(TestHttpClient(), TestOilPriceSource())
