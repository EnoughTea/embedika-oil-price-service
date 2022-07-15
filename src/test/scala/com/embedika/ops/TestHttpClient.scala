package com.embedika.ops

import java.io.InputStream
import java.net.URI

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*
import scala.language.postfixOps


final class TestHttpClient(using ioEc: IoExecutionContext) extends HttpClient:
  def close(): Unit = ()
  def get(uri: URI, timeout: FiniteDuration): Future[InputStream] = Future {
    Thread.currentThread.getContextClassLoader.getResourceAsStream("datagovru_opendata_7710349494-urals.html")
  }
