package com.embedika.ops.utils

import java.util.concurrent.{Executors, ForkJoinPool}

import scala.concurrent.ExecutionContext

import com.embedika.ops.*
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

trait PoolEnvironment extends HasEnvironment {
  override implicit val ioEc: IoExecutionContext = new IoExecutionContext(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8)))
  override implicit val cpuEc: CpuExecutionContext = new CpuExecutionContext(ExecutionContext.fromExecutor(new ForkJoinPool()))
}

trait TestEnvironment extends PoolEnvironment with ScalaFutures {
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
}

abstract class UnitSpec extends AnyFlatSpec with CommonTestFeatures with TestEnvironment
