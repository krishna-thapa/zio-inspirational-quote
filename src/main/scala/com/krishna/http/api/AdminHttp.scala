package com.krishna.http.api

import zio.ZIO
import zio.http.*
import zio.http.model.Method

import com.krishna.config.*
import com.krishna.csvStore.CsvQuoteService
import com.krishna.database.quotes.Persistence
import com.krishna.http.ConfigHttp

object AdminHttp:

  def apply(): Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "admin" / "csv-quotes" / rows =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          CsvQuoteService
            .getQuotesFromCsv(rows = getRows)
            .map(quotes => ConfigHttp.convertToJson(quotes.toList))
      case Method.GET -> !! / "admin" / "migrate"           =>
        ZIO.logInfo("Migrating quote records from CSV file to Postgres Database!") *>
          CsvQuoteService
            .migrateQuotesToDb()
            .map(result => Response.text(s"Success on migrating total $result quotes to database."))
      case req @ Method.GET -> !! / "admin" / "db-quotes"   =>
        val offset: Int = ConfigHttp.getQueryParameter(req, ("offset", 0))
        val limit: Int  = ConfigHttp.getQueryParameter(req, ("limit", 10))
        ZIO.logInfo(
          s"Retrieving all quotes from Postgres Database with offset $offset and limit $limit."
        ) *>
          (for
            results <- Persistence.runGetAllQuotes(offset, limit)
            quotes  <- results
          yield ConfigHttp.convertToJson(quotes))
    } @@ Middleware.basicAuth("admin", "admin")
