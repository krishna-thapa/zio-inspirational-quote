package com.krishna.http

import zio.http.*
import zio.http.model.Method
import zio.json.EncoderOps
import zio.{ Chunk, Scope, ZIO }

import com.krishna.config.*
import com.krishna.database.quotes.{ Persistence, QuoteDbService }
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.CsvQuoteService
import com.krishna.wikiHttp.WebClient

object AdminHttp:

  private val convertToJson: Chunk[InspirationalQuote] => Response =
    (quotes: Chunk[InspirationalQuote]) => Response.json(quotes.toJson)

  def apply(): Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "admin" / "csv-quotes" / rows                    =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          CsvQuoteService.getQuotesFromCsv(rows = getRows).map(convertToJson)
      case Method.GET -> !! / "admin" / "migrate"                              =>
        ZIO.logInfo("Migrating quote records from CSV file to Postgres Database!") *>
          CsvQuoteService
            .migrateQuotesToDb()
            .map(result => Response.text(s"Success on migrating total $result quotes to database."))
      case Method.GET -> !! / "admin" / "db-quotes" / int(offset) / int(limit) =>
        ZIO.logInfo(
          s"Retrieving all quotes Postgres Database with offset $offset and limit $limit!"
        ) *>
          (for
            results <- Persistence.runGetAllQuotes()
            quotes  <- results
          yield convertToJson(Chunk.fromIterable(quotes)))
    } @@ (Middleware.basicAuth("admin", "admin") ++ VerboseLog.log)
