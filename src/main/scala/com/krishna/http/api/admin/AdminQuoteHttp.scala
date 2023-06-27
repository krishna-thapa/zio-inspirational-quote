package com.krishna.http.api.admin

import zio.ZIO
import zio.http.HttpAppMiddleware.cors
import zio.http.*
import zio.http.model.Method

import com.krishna.auth.JwtService
import com.krishna.csvStore.CsvQuoteService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.errorHandle.ErrorHandle
import com.krishna.http.ConfigHttp
import com.krishna.model.InspirationalQuote
import com.krishna.model.user.JwtUser
import com.krishna.wikiHttp.WebClient

object AdminQuoteHttp:

  def apply(
  ): Http[QuoteRepo with WebClient, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "admin" / "csv-quotes" =>
        JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
          val getRows: Int = ConfigHttp.getQueryParameter(req, ("rows", 20))
          ZIO.logInfo(
            s"Retrieving total $getRows quotes from the CSV file data, not from Postgres DB!"
          ) *>
            CsvQuoteService
              .getQuotesFromCsv(rows = getRows)
              .map(quotes => ConfigHttp.convertToJson(quotes.toList))
        }

      case req @ Method.GET -> !! / "admin" / "migrate" =>
        JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
          ZIO.logInfo("Migrating quote records from CSV file to Postgres Database!") *>
            CsvQuoteService
              .migrateQuotesToDb()
              .map(result =>
                Response.text(s"Success on migrating total $result quotes to database.")
              )
        }

      case req @ Method.GET -> !! / "admin" / "db-quotes" =>
        JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
          val offset: Int = ConfigHttp.getQueryParameter(req, ("offset", 0))
          val limit: Int  = ConfigHttp.getQueryParameter(req, ("limit", 10))
          ZIO.logInfo(
            s"Retrieving all quotes from Postgres Database with offset $offset and limit $limit."
          ) *>
            (for
              quotes <- QuoteRepo.runGetAllQuotes(offset, limit)
              _      <- ZIO.logInfo(s"Success on retrieving total quotes: ${quotes.size}")
            yield ConfigHttp.convertToJson(quotes))
        }

      case req @ Method.GET -> !! / "admin" / "authors" =>
        JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
          ZIO.logInfo(
            "Downloading authors details from the Wiki Media API and storing in Postgres Database!"
          ) *>
            (for
              authorsInserted <- QuoteRepo.runGetAndUploadAuthorDetails()
              _ <- ZIO.logInfo(s"Success on uploading total authors: $authorsInserted")
            yield Response
              .text(s"Success on uploading total $authorsInserted authors to database."))
        }

    }
