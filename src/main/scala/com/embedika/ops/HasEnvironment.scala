package com.embedika.ops

import scala.util.Try

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import com.typesafe.config.{Config, ConfigFactory}


/** Trait for something that has a [[CpuExecutionContext]] */
trait HasCpuExecutionContext {
  implicit def cpuEc: CpuExecutionContext
}


/** Trait for something that has a [[IoExecutionContext]] */
trait HasIoExecutionContext {
  implicit def ioEc: IoExecutionContext
}


/** Provides loaded Typesafe config and its typed variant: [[AppSettings]] */
trait HasLoadedSettings {
  lazy val config: Config                  = ConfigFactory.load()
  lazy val triedSettings: Try[AppSettings] = AppSettings(config)
}


/** Provides [[ActorSystem{T}]] */
trait HasSystem[T] {
  implicit def system: ActorSystem[T]
}

/** Provides common non-invasive stuff. */
trait HasEnvironment extends HasCpuExecutionContext with HasIoExecutionContext with HasLoadedSettings


/** Provides system and its execution contexts. */
trait HasLoadedSystemEnvironment extends HasEnvironment with HasSystem[Nothing] {
  override implicit lazy val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, systemName)
  override implicit lazy val cpuEc: CpuExecutionContext = new CpuExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.default())
  )
  override lazy val ioEc: IoExecutionContext = new IoExecutionContext(
    system.dispatchers.lookup(DispatcherSelector.blocking())
  )

  def systemName: String
}
