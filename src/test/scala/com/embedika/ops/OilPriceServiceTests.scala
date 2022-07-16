package com.embedika.ops

import java.time.LocalDate

import com.embedika.ops.utils.*
import squants.market.*


final class OilPriceServiceTests extends UnitSpec {
  "An OilPriceService" should "find a single price point for an existing date" in {
    val service = makePriceService()

    val fetchNewYearPrice = service.priceAtDate(LocalDate.parse("2021-12-31"), DataGovRuOilPrices.id)

    whenReady(fetchNewYearPrice) { newYearPrice =>
      newYearPrice shouldEqual Some(Money(563.3, RUB))
    }
  }

  it should "not find a single price point for an unknown date" in {
    val service = makePriceService()

    val fetchFuturePrice = service.priceAtDate(LocalDate.parse("2099-12-31"), DataGovRuOilPrices.id)
    val fetchPastPrice   = service.priceAtDate(LocalDate.parse("1900-12-31"), DataGovRuOilPrices.id)

    whenReady(fetchFuturePrice) { price =>
      price shouldEqual None
    }
    whenReady(fetchPastPrice) { price =>
      price shouldEqual None
    }
  }

  it should "find a proper average for a monthly date range corresponding with a data point" in {
    val service = makePriceService()

    val fetchMonthlyAverage =
      service.priceInDateRange(DateRange.parse("2022-02-15", "2022-03-14").get, DataGovRuOilPrices.id)

    whenReady(fetchMonthlyAverage) { price =>
      price shouldEqual Some(Money(697.8, RUB))
    }
  }

  it should "find a proper average for a monthly date range diverging from a data point" in {
    val service = makePriceService()

    val fetchAverageUnequal =
      service.priceInDateRange(DateRange.parse("2022-03-01", "2022-04-01").get, DataGovRuOilPrices.id)

    whenReady(fetchAverageUnequal) { price =>
      price.map(_.rounded(1)) shouldEqual Some(Money(633, RUB))
    }
  }

  it should "find a proper average for an arbitrary date range corresponding with the data points" in {
    val service = makePriceService()

    val fetchArbitraryAverage =
      service.priceInDateRange(DateRange.parse("2022-02-15", "2022-06-14").get, DataGovRuOilPrices.id)

    whenReady(fetchArbitraryAverage) { price =>
      price.map(_.rounded(4)) shouldEqual Some(Money(611.9725, RUB))
    }
  }

  it should "find a min-max for an arbitrary date range" in {
    val service = makePriceService()

    val fetchArbitraryMinMax =
      service.minMaxPricesInDateRange(DateRange.parse("2022-02-25", "2022-06-06").get, DataGovRuOilPrices.id)

    whenReady(fetchArbitraryMinMax) { minMax =>
      minMax shouldEqual Some((Money(534.6, RUB), Money(697.8, RUB)))
    }
  }

  def makePriceService(): OilPriceService = new OilPriceService(new OilPriceCache(Seq(makePriceProvider())))

  def makePriceProvider(): DataGovRuOilPrices =
    new DataGovRuOilPrices(new TestOilPriceSource())
}
