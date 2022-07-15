package com.embedika.ops.utils

import java.io.InputStream
import java.net.URI

import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.language.postfixOps

import com.embedika.ops.{HttpClient, IoExecutionContext}


final class TestHttpClient(using ioEc: IoExecutionContext) extends HttpClient:
  def close(): Unit = ()
  def get(uri: URI, timeout: FiniteDuration = 1 second): Future[InputStream] = Future {
    Thread.currentThread.getContextClassLoader.getResourceAsStream("datagovru_opendata_7710349494-urals.html")
  }
