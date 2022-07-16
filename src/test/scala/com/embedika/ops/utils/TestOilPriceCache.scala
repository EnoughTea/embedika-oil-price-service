package com.embedika.ops.utils

import scala.concurrent.Future

import com.embedika.ops.*


final class TestOilPriceCache(val providers: Seq[OilPriceProvider])(implicit ioEc: IoExecutionContext)
    extends OilPriceCache {
  private val providersMap = (providers map (p => normalize(p.id) -> p)).toMap

  /** Gets cached oil price records for the specified oil price provider. */
  override def get(providerId: String): Future[Vector[OilPriceRecord]] =
    getProvider(providerId) map (_.fetchCurrent()) getOrElse Future.failed(
      new RuntimeException(s"No $providerId registered for tests")
    )

  /** Gets the specified oil price provider participating in the cache. */
  override def getProvider(providerId: String): Option[OilPriceProvider] =
    providersMap.get(normalize(providerId))

  /** Causes every participating oil price provider to load its price. */
  override def preload(): Future[Unit] = Future.successful(())
}
