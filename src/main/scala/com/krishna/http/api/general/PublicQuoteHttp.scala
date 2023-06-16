package com.krishna.http.api.general

import zio.ZIO
import zio.http.Middleware.cors
import zio.http.*
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.Method
import zio.http.model.headers.values.Origin

import com.krishna.database.quotes.QuoteRepo
import com.krishna.http.ConfigHttp
import com.krishna.model.user.JwtUser

object PublicQuoteHttp:

  // Create CORS configuration
  val config: CorsConfig =
    CorsConfig(
      allowedOrigins = s => s.contains("localhost:8080"),
      allowedMethods = Some(Set(Method.GET))
    )

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
        val maxRows: Int = rows match
          case row if row >= 10 => 10
          case row if row <= 0  => 1
          case _                => rows

        ZIO.logInfo(s"Getting $maxRows random quote from the Postgres database!") *>
          (for
            quotes <- QuoteRepo.runRandomQuote(maxRows)
            _      <- ZIO.logInfo(s"Success on getting random quote of size ${quotes.size}")
          yield ConfigHttp.convertToJson(quotes))

      case Method.GET -> !! / "quote" / "search" / searchInput =>
        ZIO.logInfo(
          s"Getting a quote from the Postgres database with searched parameter $searchInput!"
        ) *>
          (for
            quotes <- QuoteRepo.runSearchQuote(searchInput)
            _ <- ZIO.logInfo(s"Success on getting quote with searched parameter ${quotes.size}")
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
    } @@ cors(config)
