package com.krishna.http

import com.krishna.config.*
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.CsvQuoteService
import com.krishna.wikiHttp.WebClient
import zhttp.http.*
import zio.jdbc.ZConnectionPool
import zio.json.EncoderOps
import zio.{ Chunk, ZIO }

object AdminHttp:

  private val convertToJson: Chunk[InspirationalQuote] => Response =
    (quotes: Chunk[InspirationalQuote]) => Response.json(quotes.toJson)

  def apply(): Http[QuoteConfig with ZConnectionPool, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // GET /migrate
      case Method.GET -> !! / "csv-quotes" / rows  =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          CsvQuoteService.getQuotesFromCsv(rows = getRows).map(convertToJson)
      case Method.GET -> !! / "migrate" / "quotes" =>
        ZIO.logInfo("Retrieving all the quotes.....") *>
          CsvQuoteService.migrateQuotesToDb().map(_ => Response.text("Success"))
    } @@ Middleware.basicAuth("admin", "admin")
