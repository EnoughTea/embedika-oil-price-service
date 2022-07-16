package com.embedika.ops

import scala.concurrent.ExecutionContext

/** Base trait for context marks. */
trait ExectionContextBase extends ExecutionContext {
  def underlying: ExecutionContext

  override def execute(runnable: Runnable): Unit = underlying.execute(runnable)

  override def reportFailure(cause: Throwable): Unit = underlying.reportFailure(cause)
}

/** Simple wrapper used to mark an execution context used for CPU-bound operations. */
final class CpuExecutionContext(val underlying: ExecutionContext) extends ExectionContextBase

/** Simple wrapper used to mark an execution context used for IO-bound operations. */
final class IoExecutionContext(val underlying: ExecutionContext) extends ExectionContextBase
