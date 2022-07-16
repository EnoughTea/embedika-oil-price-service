package com.embedika.ops

import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Try
import scala.util.control.NonFatal

import akka.http.scaladsl.*


object Main extends App with SystemEnvironment with OilPriceServiceRoutes with HasSettings {
  val systemName             = "oil-price-service"
  val settings               = triedSettings.get
  val httpClient             = new BasicHttpClient()(ioEc)
  val dataGovRuPriceProvider = new DataGovRuOilPrices(new DataGovRuOilPriceSource(settings, httpClient))
  val oilPriceProviders      = Seq(dataGovRuPriceProvider)
  val oilPriceCache          = new OilPriceCache(oilPriceProviders, 1 hour)(ioEc)
  val service                = new OilPriceService(oilPriceCache)

  Try {
    oilPriceCache.preload()
    val http = Http()
      .newServerAt(settings.host, settings.port)
      .bindFlow(routes)
      .map { hsb =>
        logger.info("Waiting for requests at http://{}:{}/", settings.host, settings.port)
        hsb
      }
      .recoverWith { e =>
        logger.error(s"Cannot start server", e)
        Future.failed(e)
      }
    println(s"Server now listens at http://${settings.host}:${settings.port}/\nPress RETURN to stop...")
    StdIn.readLine()
    http
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  } recover { case NonFatal(e) =>
    logger.error("Error while starting server", e)
  }
}
