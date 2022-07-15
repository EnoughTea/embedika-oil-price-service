package com.embedika.ops

import java.util.concurrent.Executors

import scala.concurrent.*
import scala.concurrent.duration.*

import org.scalatest.*
import org.scalatest.time.*


final class DataGovRuOilPriceSourceTests extends UnitSpec:
  "An DataGovRuOilPriceSource" should "have a valid local price source" in {
    val priceSource = makePriceSource()

    whenReady(priceSource.local()) { prices =>
      prices.ready() shouldBe true
    }
  }

  it should "have a valid remote price source" in {
    val priceSource = makePriceSource()

    whenReady(priceSource.remote()) { prices =>
      prices.ready() shouldBe true
    }
  }

  def makePriceSource(): DataGovRuOilPriceSource =
    DataGovRuOilPriceSource(TestHttpClient())
