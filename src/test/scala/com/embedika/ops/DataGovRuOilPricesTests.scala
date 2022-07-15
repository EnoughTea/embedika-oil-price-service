package com.embedika.ops

import com.embedika.ops.utils.*


final class DataGovRuOilPricesTests extends UnitSpec:
  "A DataGovRuOilPrices" should "fetch prices from test sources" in {
    val provider = makePriceProvider()

    val fetchCurrentPrices = provider.fetchCurrent()

    whenReady(fetchCurrentPrices) { prices =>
      prices.length shouldEqual 111
    }
  }

  def makePriceProvider(): DataGovRuOilPrices =
    DataGovRuOilPrices(TestHttpClient(), TestOilPriceSource())
