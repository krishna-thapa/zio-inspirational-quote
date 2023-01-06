package com.krishna.http

import zio.{Chunk, ULayer}
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*
import zio.json.EncoderOps
import com.krishna.config.QuoteAndDbConfig
import com.krishna.database.quotes.Persistence
import com.krishna.http.api.*
import com.krishna.model.InspirationalQuote

object ConfigHttp:

  private val port: Int = 9000

  val combinedHttp: Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    HomePage() ++ AdminHttp() ++ UserHttp()

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(5)

  val configLayer: ULayer[ServerConfig] = ServerConfig.live(config)

  private[http] def convertToJson(quotes: Chunk[InspirationalQuote]): Response =
    Response.json(quotes.toJson)
