package com.krishna.http

import pdi.jwt.JwtClaim
import zio.*
import zio.http.HttpAppMiddleware.cors
import zio.http.*
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.{ Headers, Method, Status }
import zio.json.{ EncoderOps, JsonEncoder }

import com.krishna.auth.JwtService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.http.api.*
import com.krishna.http.api.admin.*
import com.krishna.http.api.general.*
import com.krishna.http.api.user.*
import com.krishna.model.user.JwtUser
import com.krishna.wikiHttp.WebClient

object ConfigHttp:

  private val port: Int = 9000

  private type AllRepo = QuoteRepo with UserRepo with WebClient

  private val combinedHttps: HttpApp[AllRepo, Throwable] =
    HomePage() ++
      PublicAuthHttp.apply() ++ PublicQuoteHttp.apply() ++
      UserQuoteHttp.apply() ++ UserAuthHttp.apply() ++
      AdminAuthHttp.apply() ++ AdminQuoteHttp.apply()

  val httpsWithMiddlewares =
    combinedHttps @@ MiddlewareConfig.middlewares @@ cors(MiddlewareConfig.configs)

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    // To upload and download the image, have to increase the request size
    .objectAggregator(2097152)
    // .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(5)

  val configLayer: ULayer[ServerConfig] = ServerConfig.live(config)

  // ==================== Helper methods ==========================
  def convertToJson[T](quote: T)(using JsonEncoder[T]): Response =
    Response.json(quote.toJson)

  private[http] def getQueryParameter(request: Request, parameterWithDefault: (String, Int)): Int =
    request
      .url
      .queryParams
      .get(parameterWithDefault._1)
      .flatMap(_.apply(0).toIntOption)
      .getOrElse(parameterWithDefault._2)
