package com.embedika.ops

import java.util.Locale

import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.Success

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}


/** Trait for something capable of caching provider's oil price records
  *  to avoid querying remote oil price sources all the time.
  */
trait OilPriceCache {

  /** Gets cached oil price records for the specified oil price provider. */
  def get(providerId: String): Future[Vector[OilPriceRecord]]

  /** Gets the specified oil price provider participating in the cache. */
  def getProvider(providerId: String): Option[OilPriceProvider]

  /** Causes every participating oil price provider to load its price. */
  def preload(): Future[Unit]

  protected def normalize(providerId: String): String = providerId.toLowerCase(Locale.ROOT)
}


/** Caches provider's current oil prices for a given time using a thin Scala wrapper for Caffeine
  * (https://github.com/ben-manes/caffeine).
  */
final class ScaffeineOilPriceCache(providers: Seq[OilPriceProvider], ttl: FiniteDuration = 1 hour)(implicit
    ioEc: IoExecutionContext
) extends OilPriceCache {
  private val providersMap = (providers map (p => normalize(p.id) -> p)).toMap

  private val cache: AsyncLoadingCache[String, Vector[OilPriceRecord]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(ttl)
      .maximumSize(1)
      .buildAsyncFuture(currentPricesFromProvider)

  override def get(providerId: String): Future[Vector[OilPriceRecord]] = cache.get(normalize(providerId))

  override def getProvider(providerId: String): Option[OilPriceProvider] = providersMap.get(normalize(providerId))

  override def preload(): Future[Unit] = {
    val preloadTasks = providersMap map { case (id, _) => get(id) }
    Future.sequence(preloadTasks map (_ transform (Success(_)))) map (_ => ())
  }

  private def currentPricesFromProvider(providerId: String): Future[Vector[OilPriceRecord]] =
    providersMap.get(normalize(providerId)) map (_.fetchCurrent()) getOrElse Future.failed(
      new RuntimeException(s"Provider with name $providerId cannot be found")
    )
}
