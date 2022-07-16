package com.embedika.ops

import scala.concurrent.duration.*
import scala.language.postfixOps
import com.embedika.ops.utils.*


final class OilPriceCacheTests extends UnitSpec {
  "An OilPriceCache" should "fetch existing prices from provider" in {
    val dataGovRuPrices = makePriceProvider()
    val cache = new OilPriceCache(Seq(dataGovRuPrices))

    val fetchCurrentPrices = cache.get(dataGovRuPrices.id)

    whenReady(fetchCurrentPrices) { currentPrices =>
      currentPrices.length shouldEqual 111
    }
  }

  it should "update expired prices from provider automatically" in {
    val dataGovRuPrices = makePriceProvider()
    val cache = new OilPriceCache(Seq(dataGovRuPrices), 1 milli)

    cache.get(dataGovRuPrices.id)
    Thread.sleep(5)
    val fetchCurrentPrices = cache.get(dataGovRuPrices.id)

    whenReady(fetchCurrentPrices) { currentPrices =>
      currentPrices.length shouldEqual 111
    }
  }

  def makePriceProvider(): DataGovRuOilPrices =
    new DataGovRuOilPrices(new TestHttpClient(), new TestOilPriceSource())
}
