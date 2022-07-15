package com.embedika.ops

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*
import scala.language.postfixOps

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}


/** Caches current oil prices for a given time to avoid querying remote oil price sources all the time. */
final class OilPriceCache(providers: Seq[OilPriceProvider], ttl: FiniteDuration = 1 hour)(using
    ioEc: IoExecutionContext
):
  private val providersMap = (providers map (p => p.id -> p)).toMap

  private val cache: AsyncLoadingCache[String, Vector[OilPriceRecord]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(ttl)
      .maximumSize(1)
      .buildAsyncFuture(currentPricesFromProvider)

  def get(providerId: String): Future[Vector[OilPriceRecord]] = cache.get(providerId)

  private def currentPricesFromProvider(providerId: String): Future[Vector[OilPriceRecord]] =
    providersMap.get(providerId) map (_.fetchCurrent()) getOrElse Future.failed(
      new RuntimeException(s"Provider with name $providerId cannot be found")
    )
