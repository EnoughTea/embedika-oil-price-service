package com.embedika.ops

import akka.http.caching.LfuCache
import akka.http.caching.scaladsl.{Cache, CachingSettings}
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.{RequestContext, RouteResult}
import com.typesafe.config.Config


trait RequestCache {
  def config: Config

  /** Use the request's URI as the cache's key */
  val keyerFunction: PartialFunction[RequestContext, Uri] = { case r: RequestContext =>
    r.request.uri
  }
  lazy val cachingSettings: CachingSettings = CachingSettings(config)
  lazy val lfuCache: Cache[Uri, RouteResult] = LfuCache(cachingSettings)
}
