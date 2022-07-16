package com.embedika.ops

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Try, Using}
import scala.util.control.NonFatal

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.*


object Main extends App with SystemEnvironment with OilPriceServiceRoutes with HasSettings {
  val systemName             = "oil-price-service"
  val settings               = triedSettings.get
  val httpClient             = new BasicHttpClient()(ioEc)
  val dataGovRuPriceProvider = new DataGovRuOilPrices(new DataGovRuOilPriceSource(settings, httpClient))
  val oilPriceProviders      = Seq(dataGovRuPriceProvider)
  val service                = new OilPriceService(new OilPriceCache(oilPriceProviders)(ioEc))

  Try {
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
