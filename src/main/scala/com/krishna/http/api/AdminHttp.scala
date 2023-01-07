package com.krishna.http.api

import pdi.jwt.JwtClaim
import zio.ZIO
import zio.http.*
import zio.http.model.Method

import com.krishna.config.*
import com.krishna.csvStore.CsvQuoteService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.http.ConfigHttp

object AdminHttp:

  def apply(
    claim: JwtClaim
  ): Http[QuoteRepo with QuoteAndDbConfig, Throwable, Request, Response] =
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
            quotes <- QuoteRepo.runGetAllQuotes(offset, limit)
            _      <- ZIO.logInfo(s"Success on retrieving total quotes: ${quotes.size}")
          yield ConfigHttp.convertToJson(quotes))
    }
