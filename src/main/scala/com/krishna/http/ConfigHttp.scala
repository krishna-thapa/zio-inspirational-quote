package com.krishna.http

import zio.ULayer
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*
import zio.json.{ EncoderOps, JsonEncoder }

import com.krishna.config.QuoteAndDbConfig
import com.krishna.database.quotes.Persistence
import com.krishna.http.api.*

object ConfigHttp:

  private val port: Int = 9000
  //Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response]
  val combinedHttp: Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    HomePage() ++ AdminHttp() ++ UserHttp()

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(5)

  val configLayer: ULayer[ServerConfig] = ServerConfig.live(config)

  // ==================== Helper methods ==========================
  private[http] def convertToJson[T](quote: T)(using JsonEncoder[T]): Response =
    Response.json(quote.toJson)

  private[http] def getQueryParameter(request: Request, parameterWithDefault: (String, Int)): Int =
    request
      .url
      .queryParams
      .get(parameterWithDefault._1)
      .flatMap(_.apply(0).toIntOption)
      .getOrElse(parameterWithDefault._2)
