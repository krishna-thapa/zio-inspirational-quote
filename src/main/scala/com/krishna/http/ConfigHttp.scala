package com.krishna.http

import pdi.jwt.JwtClaim
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*
import zio.json.{ EncoderOps, JsonEncoder }
import zio.{ ULayer, ZIO }

import com.krishna.auth.JwtService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.http.api.*

object ConfigHttp:

  private val port: Int = 9000

  private type AllRepo = QuoteRepo with UserRepo

  private val jwtAuthHttps: JwtClaim => Http[QuoteRepo, Throwable, Request, Response] = claim =>
    AdminHttp.apply(claim) ++ UserHttp.apply(claim)

  val combinedHttps: Http[AllRepo, Throwable, Request, Response] =
    HomePage() ++ AuthHttp() ++
      JwtService.authenticate(jwtAuthHttps)

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
