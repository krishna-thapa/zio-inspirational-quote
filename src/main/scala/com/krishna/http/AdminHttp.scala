package com.krishna.http

import zhttp.http.*
import zio.json.EncoderOps
import zio.{ Chunk, Scope, ZIO }

import com.krishna.config.*
import com.krishna.database.quotes.Persistence
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.CsvQuoteService
import com.krishna.wikiHttp.WebClient

object AdminHttp:

  private val convertToJson: Chunk[InspirationalQuote] => Response =
    (quotes: Chunk[InspirationalQuote]) => Response.json(quotes.toJson)

  def apply(): Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      // GET /migrate
      case Method.GET -> !! / "csv-quotes" / rows  =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          CsvQuoteService.getQuotesFromCsv(rows = getRows).map(convertToJson)
      case Method.GET -> !! / "migrate" / "quotes" =>
        ZIO.logInfo("Retrieving all the quotes.....") *>
          CsvQuoteService
            .migrateQuotesToDb()
            .map(result => Response.text(s"Success on migrating total $result quotes to database."))
    } @@ (Middleware.basicAuth("admin", "admin") ++ VerboseLog.log)
