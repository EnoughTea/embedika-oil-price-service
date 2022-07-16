package com.embedika.ops.utils

import java.util.concurrent.{Executors, ForkJoinPool}

import scala.concurrent.ExecutionContext

import com.embedika.ops.{CpuExecutionContext, IoExecutionContext}
import org.scalatest.*
import org.scalatest.concurrent.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.*
import org.scalatest.time.*


trait CommonTestFeatures
    extends should.Matchers
    with OptionValues
    with TryValues
    with Inside
    with Inspectors
    with ScalaFutures


trait TestImplicits extends ScalaFutures {
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
  implicit val ioEc: IoExecutionContext = new IoExecutionContext(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8)))
  implicit val cpuEc: CpuExecutionContext = new CpuExecutionContext(ExecutionContext.fromExecutor(new ForkJoinPool()))
}

abstract class UnitSpec extends AnyFlatSpec with CommonTestFeatures with TestImplicits
