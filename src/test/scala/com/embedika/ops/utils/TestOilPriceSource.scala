package com.embedika.ops.utils

import java.io.InputStreamReader

import scala.concurrent.Future

import com.embedika.ops.{IoExecutionContext, OilPriceSource}


/** Reads prices from JAR .csv resource. */
final class TestOilPriceSource extends OilPriceSource {
  override def remote()(implicit ec: IoExecutionContext): Future[InputStreamReader] = local()

  override def local()(implicit ec: IoExecutionContext): Future[InputStreamReader] = Future {
    scala.io.Source.fromResource("data-20220617T1317-structure-20210419T0745.csv").reader()
  }
}
