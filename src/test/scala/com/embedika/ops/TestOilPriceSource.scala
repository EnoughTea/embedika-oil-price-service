package com.embedika.ops

import java.io.InputStreamReader
import java.net.URI

import scala.concurrent.Future


/** Reads prices from JAR .csv resource. */
final class TestOilPriceSource extends OilPriceSource:
  override def remote()(using ec: IoExecutionContext): Future[InputStreamReader] = local()

  override def local()(using ec: IoExecutionContext): Future[InputStreamReader] = Future {
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
  }
