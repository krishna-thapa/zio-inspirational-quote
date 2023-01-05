package com.krishna.http

import zio.ULayer
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*

import com.krishna.config.QuoteAndDbConfig
import com.krishna.database.quotes.Persistence

object ConfigHttp:

  private val port: Int = 9000

  val combinedHttp: Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    HomePage() ++ AdminHttp()

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(1)

  val configLayer: ULayer[ServerConfig] = ServerConfig.live(config)
