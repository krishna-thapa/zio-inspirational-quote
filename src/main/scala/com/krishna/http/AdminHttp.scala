package com.krishna.http

import zhttp.http.*
import zio.ZIO
import zio.json.EncoderOps

import com.krishna.config.EnvironmentConfig
import com.krishna.readCsv.ReadQuoteCsv
import com.krishna.wikiHttp.WebClient

object AdminHttp:
  def apply(): Http[WebClient with EnvironmentConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // GET /migrate
      case Method.GET -> !! / "migrate" =>
        ZIO.logInfo("Retrieving all the quotes.....") *>
          ReadQuoteCsv.getQuotesFromCsv.map(quotes => Response.json(quotes.toJson))
    } @@ Middleware.basicAuth("admin", "admin")
