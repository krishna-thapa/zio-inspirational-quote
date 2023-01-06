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
      case Method.GET -> !! / "quote" / "random" =>
        ZIO.logInfo("Getting a random quote from the Postgres database!") *>
          (for
            results <- Persistence.runRandomQuote()
            quotes  <- results
          yield ConfigHttp.convertToJson(Chunk.fromIterable(quotes)))
    } @@ VerboseLog.log
