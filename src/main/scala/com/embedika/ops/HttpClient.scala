package com.embedika.ops

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URI

import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.language.postfixOps

import com.typesafe.scalalogging.StrictLogging
import sttp.client3.*
import sttp.model.Uri


/** Trait for a simple HTTP client capable of GETting something from the web. */
trait HttpClient extends AutoCloseable {
  def get(uri: URI, timeout: FiniteDuration = 5 seconds): Future[InputStream]
}


/** Performs a simple HTTP GET request for an optional task of downloading fresh oil prices.
  * Uses STTP (https://sttp.softwaremill.com/en/latest/) mostly because akka-http client is too limited,
  * it does not even support request timeouts out of the box atm: https://github.com/akka/akka-http/issues/42
  */
final class BasicHttpClient(implicit ec: IoExecutionContext) extends HttpClient with StrictLogging {
  private val backend = HttpClientFutureBackend()

  /** Fires a single HttpRequest to the given URI, returns response body as an InputStream. */
  def get(uri: URI, timeout: FiniteDuration = 5 seconds): Future[InputStream] = {
    logger.trace(s"Sending GET request to $uri with timeout $timeout")
    val request = basicRequest.get(Uri(uri)).readTimeout(timeout)
    request.send(backend) flatMap { response =>
      response.body fold (
        e => Future.failed(new RuntimeException(e)),
        body => Future(new ByteArrayInputStream(body.getBytes("UTF-8")))
      )
    }
  }

  def close(): Unit = backend.close()
}
