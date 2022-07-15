package com.embedika.ops

import com.embedika.ops.utils.*


final class DataGovRuOilPriceSourceTests extends UnitSpec with HasSettings:
  "An DataGovRuOilPriceSource" should "have a valid local price source" in {
    val priceSource = makePriceSource()

    val fetchLocalPrices = priceSource.local()

    whenReady(fetchLocalPrices) { prices =>
      prices.ready() shouldBe true
    }
  }

  it should "have a valid remote price source" in {
    val priceSource = makePriceSource()

    val fetchRemotePrices = priceSource.local()

    whenReady(fetchRemotePrices) { prices =>
      prices.ready() shouldBe true
    }
  }

  def makePriceSource(): DataGovRuOilPriceSource =
    DataGovRuOilPriceSource(triedSettings.get, TestHttpClient())
