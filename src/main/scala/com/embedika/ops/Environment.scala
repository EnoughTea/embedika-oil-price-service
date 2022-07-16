package com.embedika.ops

import scala.util.Try

import akka.actor.typed.scaladsl.*
import akka.actor.typed.*
import com.typesafe.config.{Config, ConfigFactory}


trait HasCpuExecutionContext {
  implicit def cpuEc: CpuExecutionContext
}


trait HasIoExecutionContext {
  implicit def ioEc: IoExecutionContext
}


trait HasSettings {
  lazy val config: Config                  = ConfigFactory.load()
  lazy val triedSettings: Try[AppSettings] = AppSettings(config)
}


trait HasSystem[T] extends AutoCloseable {
  implicit def system: ActorSystem[T]

  override def close(): Unit = {
    super.close()
    system.terminate()
  }
}

trait Environment extends HasCpuExecutionContext with HasIoExecutionContext with HasSettings

trait SystemEnvironment extends Environment with HasSystem[Nothing] {
  override implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, systemName)
  override implicit val cpuEc: CpuExecutionContext = new CpuExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.blocking())
  )
  override implicit val ioEc: IoExecutionContext = new IoExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.default())
  )

  def systemName: String
}
