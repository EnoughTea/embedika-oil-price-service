package com.embedika.ops

import java.time.LocalDate

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.*
import akka.http.scaladsl.server.Directives.*
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto.*
import squants.market.Money


trait OilPriceServiceRoutes extends Routes with FailFastCirceSupport with OilPriceRecordJsonFormat {
  def service: OilPriceService
  implicit def cpuEc: CpuExecutionContext

  registerRoute {

    pathPrefix("prices" / Segment) { providerId =>
      get {
        concat(
          path("all") {
            val eventualResponse = service.allRecords(providerId) map (ApiResponse.entity(_))
            ApiResponse.completeWith(eventualResponse)
          },
          path("stats") {
            final case class RecordStats(total: Long)
            val eventualResponse = service.allRecords(providerId) map { records =>
              EntityResponse(RecordStats(records.length))
            }
            ApiResponse.completeWith(eventualResponse)
          },
          (path("single") & parameterMap) { params =>
            val eventualResponse = withLocalDateParam("date", params) { date =>
              service.priceAtDate(date, providerId) map {
                case Some(price) => ApiResponse.entity(price)
                case None        => ApiResponse.notFound(s"No oil price records exist for $date")
              }
            }
            ApiResponse.completeWith(eventualResponse)
          },
          (path("averageOver") & parameterMap) { params =>
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
