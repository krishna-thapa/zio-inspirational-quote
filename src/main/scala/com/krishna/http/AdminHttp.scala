package com.krishna.http

import zhttp.http.*
import zio.json.EncoderOps
import zio.{ Chunk, ZIO }

import com.krishna.config.*
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.ReadQuoteCsv
import com.krishna.wikiHttp.WebClient

object AdminHttp:

  private val convertToJson: Chunk[InspirationalQuote] => Response = (quotes: Chunk[InspirationalQuote]) =>
    Response.json(quotes.toJson)

  def apply(): Http[WebClient with QuoteAndWikiConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // GET /migrate
      case Method.GET -> !! / "csv-quotes" / rows =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          ReadQuoteCsv.getQuotesFromCsv(rows = getRows).map(convertToJson)
      case Method.GET -> !! / "migrate" / "quotes" =>
        ZIO.logInfo("Retrieving all the quotes.....") *>
          ReadQuoteCsv.getQuotesFromCsv(isMigrateAll = true).map(convertToJson)
    } @@ Middleware.basicAuth("admin", "admin")
