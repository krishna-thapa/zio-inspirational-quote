package com.krishna.http.api

import zio.ZIO
import zio.http.*
import zio.http.model.Method
import com.krishna.config.*
import com.krishna.csvStore.CsvQuoteService
import com.krishna.database.quotes.Persistence
import com.krishna.http.ConfigHttp
import pdi.jwt.JwtClaim

object AdminHttp:

  def apply(
    claim: JwtClaim
  ): Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {

      case Method.GET -> !! / "admin" / "csv-quotes" / rows =>
        val getRows: Option[Int] = rows.toIntOption
        ZIO.logInfo(s"Retrieving total $rows quotes from the CSV file data!") *>
          CsvQuoteService
            .getQuotesFromCsv(rows = getRows)
            .map(quotes => ConfigHttp.convertToJson(quotes.toList))

      case Method.GET -> !! / "admin" / "migrate" =>
        ZIO.logInfo("Migrating quote records from CSV file to Postgres Database!") *>
          CsvQuoteService
            .migrateQuotesToDb()
            .map(result => Response.text(s"Success on migrating total $result quotes to database."))

      case req @ Method.GET -> !! / "admin" / "db-quotes" =>

        val offset: Int = ConfigHttp.getQueryParameter(req, ("offset", 0))
        val limit: Int  = ConfigHttp.getQueryParameter(req, ("limit", 10))
        ZIO.logInfo(
          s"Retrieving all quotes from Postgres Database with offset $offset and limit ${claim.content}."
        ) *>
          (for
            results <- Persistence.runGetAllQuotes(offset, limit)
            quotes  <- results
            _       <- ZIO.logInfo(s"Success on retrieving total quotes: ${quotes.size}")
          yield ConfigHttp.convertToJson(quotes))
    }
