package com.embedika.ops

import java.time.LocalDate

import scala.concurrent.Future
import scala.util.*

import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.*
import akka.http.scaladsl.server.Directives.*
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto.*


trait OilPriceServiceRoutes extends Routes with FailFastCirceSupport with OilPriceRecordJsonFormat {
  def service: OilPriceService
  implicit def cpuEc: CpuExecutionContext

  registerRoute {

    pathPrefix("prices" / Segment) { providerId =>
      get {
        concat(
          path("all") {
            val eventualResponse = service.allRecords(providerId) map (EntityResponse(_))
            ApiResponse.completeWith(eventualResponse)
          },
        )
      }
    }

  }
}
