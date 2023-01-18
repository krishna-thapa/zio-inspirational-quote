package com.krishna.http

import pdi.jwt.JwtClaim
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.{ Server, * }
import zio.json.{ EncoderOps, JsonEncoder }
import zio.{ ULayer, ZIO }
import com.krishna.auth.JwtService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.http.api.*
import com.krishna.http.api.admin.*
import com.krishna.http.api.user.*
import com.krishna.http.api.general.*
import com.krishna.model.user.JwtUser

object ConfigHttp:

  private val port: Int = 9000

  private type AllRepo = QuoteRepo with UserRepo

  private val jwtUserHttps: JwtUser => Http[AllRepo, Throwable, Request, Response] = claim =>
    UserQuoteHttp.apply(claim) ++ UserAuthHttp(claim)

  private val jwtAdminHttps: JwtUser => Http[AllRepo, Throwable, Request, Response] = claim =>
    AdminAuthHttp(claim) ++ AdminQuoteHttp.apply(claim)

  val combinedHttps: Http[AllRepo, Throwable, Request, Response] =
    HomePage() ++ PublicAuthHttp() ++ PublicQuoteHttp() //++
     // JwtService.authenticateUser(jwtUserHttps) ++
     // JwtService.authenticateUser(jwtAdminHttps, isAdmin = true)

//  OpenAPI.apply(
//    info = Info(
//      title = "title",
//      version = "version",
//      description = Doc.p("description"),
//      termsOfService = new URI("https://google.com"),
//      contact = None,
//      license = None,
//    ),
//    servers = Server.install(ConfigHttp.combinedHttps)
//  )

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    .objectAggregator(2097152)
    .leakDetection(LeakDetectionLevel.PARANOID)
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
