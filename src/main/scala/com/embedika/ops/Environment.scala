package com.embedika.ops

import scala.util.Try

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
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


trait HasSystem[T] {
  implicit def system: ActorSystem[T]
}

trait Environment extends HasCpuExecutionContext with HasIoExecutionContext with HasSettings


trait SystemEnvironment extends Environment with HasSystem[Nothing] {
  override implicit lazy val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, systemName)
  override implicit lazy val cpuEc: CpuExecutionContext = new CpuExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.blocking())
  )
  override lazy val ioEc: IoExecutionContext = new IoExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.default())
  )

  def systemName: String
}
