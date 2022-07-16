package com.embedika.ops

import scala.collection.mutable
import scala.util.Random

import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.*
import akka.http.scaladsl.server.directives.CachingDirectives.cache
import com.typesafe.scalalogging.LazyLogging


/** Provides all routes for a server. Wraps all registered routes with utility directives. */
trait Routes extends Directives with RequestCache with LazyLogging {
  private val routesBuffer = new mutable.ArrayBuffer[Route]()
  private val unhandledExceptionHandler = ExceptionHandler { case e =>
    logger.error("Unhandled exception occured in routes", e)
    complete(HttpResponse(StatusCodes.InternalServerError, Nil, s"Unhandled exception: ${e.getMessage}"))
  }

  def settings: AppSettings

  /** Gets all available routes to serve at a later point. */
  def routes: Route = cache(lfuCache, keyerFunction) {
    mapRequest(setRequestId) {
      redirectToNoTrailingSlashIfPresent(StatusCodes.MovedPermanently) {
        handleExceptions(unhandledExceptionHandler) {
          headerValueByName(settings.requestIdHeaderName) { requestId =>
            respondWithHeader(RawHeader(settings.requestIdHeaderName, requestId)) {
              routesBuffer reduce (_ ~ _)
            }
          }
        }
      }
    }
  }

  /** Addes a new route to [[routes]]. */
  def registerRoute(route: Route): Unit = routesBuffer += route

  private def setRequestId(req: HttpRequest): HttpRequest = {
    val requestId = Random.alphanumeric.take(5).mkString
    req.addHeader(RawHeader(settings.requestIdHeaderName, requestId))
  }
}
