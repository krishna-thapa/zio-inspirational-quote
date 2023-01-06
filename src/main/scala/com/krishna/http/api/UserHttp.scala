package com.krishna.http.api

import com.krishna.config.QuoteAndDbConfig
import com.krishna.database.quotes.Persistence
import com.krishna.http.{ ConfigHttp, VerboseLog }
import zio.{ Chunk, ZIO }
import zio.http.*
import zio.http.model.Method

object UserHttp:

  /*
    1. Random quote
    2. 10 random quotes
    3. Selected quote by UUID
    4. Random quote by genre
   */

  def apply(): Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "quote" / "random" =>
        val rows: Int    = ConfigHttp.getQueryParameter(req, ("rows", 1))
        val maxRows: Int = if rows >= 10 then 10 else rows
        ZIO.logInfo(s"Getting $maxRows random quote from the Postgres database!") *>
          (for
            results <- Persistence.runRandomQuote(maxRows)
            quotes  <- results
          yield ConfigHttp.convertToJson(Chunk.fromIterable(quotes)))
      case Method.GET -> !! / "quote" / uuid(uuid)     =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
          (for
            results <- Persistence.runSelectQuote(uuid)
            quote   <- results
          yield ConfigHttp.convertToJson(quote))
    } @@ VerboseLog.log
