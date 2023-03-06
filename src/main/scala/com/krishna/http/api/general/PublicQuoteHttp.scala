package com.krishna.http.api.general

import zio.ZIO
import zio.http.*
import zio.http.model.Method

import com.krishna.database.quotes.QuoteRepo
import com.krishna.http.ConfigHttp
import com.krishna.model.user.JwtUser

object PublicQuoteHttp:

  def apply(): Http[QuoteRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "quote" / "quoteOfTheDay" =>
        ZIO.logInfo(s"Getting a quote of the day!") *>
          (for
            quotes <- QuoteRepo.runQuoteOfTheDayQuote()
            _      <- ZIO.logInfo(s"Success on getting quote of the day!")
          yield ConfigHttp.convertToJson(quotes))

      case req @ Method.GET -> !! / "quote" / "random" =>
        val rows: Int    = ConfigHttp.getQueryParameter(req, ("rows", 1))
        val maxRows: Int = if rows >= 10 then 10 else rows
        ZIO.logInfo(s"Getting $maxRows random quote from the Postgres database!") *>
          (for
            quotes <- QuoteRepo.runRandomQuote(maxRows)
            _      <- ZIO.logInfo(s"Success on getting random quote of size $maxRows")
          yield ConfigHttp.convertToJson(quotes))

      case Method.GET -> !! / "quote" / "genre" / genre =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with genre type $genre!") *>
          (for
            quotes <- QuoteRepo.runSelectGenreQuote(genre)
            _ <- ZIO.logInfo(s"Success on getting quote with genre $genre, size: ${quotes.size}")
          yield ConfigHttp.convertToJson(quotes))

      case Method.GET -> !! / "quote" / "genre-title" / term =>
        ZIO.logInfo(s"Getting a genre titles from the Postgres database that starts with $term!") *>
          (for
            genres <- QuoteRepo.runSelectGenreTitles(term)
            _      <- ZIO.logInfo(
              s"Success on getting genre titles starting $term, size: ${genres.size}"
            )
          yield ConfigHttp.convertToJson(genres))
    }
