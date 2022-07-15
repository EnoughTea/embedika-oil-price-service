package com.embedika.ops

import java.util.concurrent.Executors

import scala.concurrent.*
import scala.concurrent.duration.*

import org.scalatest.*
import org.scalatest.time.*


class DataGovRuOilPriceSourceTest extends UnitSpec:
  given defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
  given ioEc: IoExecutionContext = IoExecutionContext(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8)))

  "An DataGovRuOilPriceSource" should "have a valid local price source" in {
    val priceSource = makePriceSource()

    whenReady(priceSource.local()) { prices =>
      prices.ready() shouldBe true
    }
  }

  it should "have a valid remote price source" in {
    val priceSource = makePriceSource()

    whenReady(priceSource.local()) { prices =>
      prices.ready() shouldBe true
    }
  }

  def makePriceSource(): DataGovRuOilPriceSource = new DataGovRuOilPriceSource()
