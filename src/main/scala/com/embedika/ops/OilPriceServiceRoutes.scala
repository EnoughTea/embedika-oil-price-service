package com.embedika.ops

import java.time.LocalDate

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.util.Properties.lineSeparator

import akka.http.scaladsl.server.*
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto.*


/** Contains actual oil price service HTTP routes. */
trait OilPriceServiceRoutes extends Routes with FailFastCirceSupport with OilPriceRecordJsonFormat {
  def oilPriceCache: OilPriceCache
  def oilPriceProviders: Vector[OilPriceProvider]
  def service: OilPriceService

  implicit def cpuEc: CpuExecutionContext

  // Poor man's replacement for Swagger:
  private val helloMessage: String =
    Seq(
      "Welcome to the oil price service. Try one of the following routes:",
      "GET /prices/stats — gets a total count of current oil price records for all providers.",
      "GET /prices/:providerId/all — gets a list of provider's current oil prices.",
      "GET /prices/:providerId/single?date=2020-12-31 — gets provider's oil price for the given day.",
      "GET /prices/:providerId/averageOver?startDate=2020-12-31&endDate=2021-01-01 — " +
        "gets provider's oil price averaged over the given date range.",
      "GET /prices/:providerId/minmax?startDate=2020-12-31&endDate=2021-01-01 — " +
        "gets provider's minimum and maxium prices in the given date range.",
      s"${lineSeparator}Currently, only data.gov.ru is available as a :providerId"
    ) mkString lineSeparator

  private val pricesMessage: String = "You should specify provider in the path, eg: " +
    "/prices/data.gov.ru/averageOver?startDate=2020-12-31&endDate=2021-01-01"

  private val providerIdCheck: ValidatedProviderId = new ValidatedProviderId(oilPriceCache)
  private val wrongProviderIdHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle { case MalformedPathSegmentRejection(_, msg) => complete(ApiResponse.notFound(msg).toHttpResponse) }
    .result()

  registerRoute {
    (pathEndOrSingleSlash & get) {
      complete(ApiResponse.ok(helloMessage).toHttpResponse)
    }
  }

  registerRoute {
    (path("prices") & pathEndOrSingleSlash & get) {
      complete(ApiResponse.notFound(pricesMessage).toHttpResponse)
    }
  }

  registerRoute {
    (path("prices" / "stats") & get) {
      final case class OilPriceProviderStats(providerId: String, totalOilPriceRecords: Long)
      logger.debug("Incoming HTTP request for all price providers stats.")
      val eventualStats = oilPriceProviders map { provider =>
        oilPriceCache.get(provider.id) map (recs => OilPriceProviderStats(provider.id, recs.length))
      }
      val eventualResponse = Future.sequence(eventualStats) map { EntityResponse(_) }
      ApiResponse.completeWith(eventualResponse)
    }
  }

  registerRoute {

    (handleRejections(wrongProviderIdHandler) & providerIdCheck(pathPrefix("prices" / Segment))) { providerId =>
      get {
        concat(
          path("all") {
            logger.debug("Incoming HTTP request for all oil price records.")
            val eventualResponse = service.allRecords(providerId) map (ApiResponse.entity(_))
            ApiResponse.completeWith(eventualResponse)
          },
          (path("single") & parameterMap) { params =>
            logger.debug(s"Incoming HTTP request for a single oil price record, params: $parameterMap")
            val eventualResponse = withLocalDateParam("date", params) { date =>
              service.priceAtDate(date, providerId) map {
                case Some(price) => ApiResponse.entity(price)
                case None        => ApiResponse.notFound(s"No oil price records exist for $date")
              }
            }
            ApiResponse.completeWith(eventualResponse)
          },
          (path("averageOver") & parameterMap) { params =>
            logger.debug(s"Incoming HTTP request for a price averaged over dates, params: $parameterMap")
            val eventualResponse = withLocalDateParam("startDate", params) { startDate =>
              withLocalDateParam("endDate", params) { endDate =>
                val dateRange = DateRange(startDate, endDate)
                service.priceInDateRange(dateRange, providerId) map {
                  case Some(price) => ApiResponse.entity(price)
                  case None =>
                    ApiResponse.notFound(s"Given date range $dateRange is disjointed with all oil price records")
                }
              }
            }
            ApiResponse.completeWith(eventualResponse)
          },
          (path("minmax") & parameterMap) { params =>
            logger.debug(s"Incoming HTTP request for a min-max price for the dates, params: $parameterMap")
            val eventualResponse = withLocalDateParam("startDate", params) { startDate =>
              withLocalDateParam("endDate", params) { endDate =>
                val dateRange = DateRange(startDate, endDate)
                service.minMaxPricesInDateRange(dateRange, providerId) map {
                  case Some(prices) => ApiResponse.entity(prices)
                  case None =>
                    ApiResponse.notFound(s"Given date range $dateRange is disjointed with all oil price records")
                }
              }
            }
            ApiResponse.completeWith(eventualResponse)
          }
        )
      }
    }

  }

  def withLocalDateParam(paramName: String, params: Map[String, String])(
      func: LocalDate => Future[ApiResponse]
  ): Future[ApiResponse] = {
    val maybeTriedDate = params.get(paramName).map(dateRepr => Try(LocalDate.parse(dateRepr)))
    maybeTriedDate match {
      case Some(triedDate) =>
        triedDate match {
          case Failure(_) =>
            Future.successful(
              ApiResponse.badRequest(
                s"$paramName does not contain a valid date, should be ISO local date text like \"2007-12-03\""
              )
            )
          case Success(date) => func(date)
        }
      case None => Future.successful(ApiResponse.badRequest(s"$paramName is missing from parameters"))
    }
  }
}

final case class MalformedPathSegmentRejection(segmentName: String, errorMsg: String) extends Rejection


final class ValidatedProviderId(oilPriceCache: OilPriceCache) {
  def apply(directive: Directive1[String]): Directive1[String] =
    directive.filter(
      id => oilPriceCache.getProvider(id).nonEmpty,
      MalformedPathSegmentRejection(":providerId", "Got non-existent oil price provider id")
    )
}
