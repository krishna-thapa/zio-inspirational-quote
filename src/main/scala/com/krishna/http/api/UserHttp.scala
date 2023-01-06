package com.krishna.http.api

import java.util.UUID
import zio.ZIO
import zio.http.*
import zio.http.model.Method
import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.Persistence
import com.krishna.http.ConfigHttp
import pdi.jwt.JwtClaim

object UserHttp:

  def apply(claim: JwtClaim): Http[Persistence with DatabaseConfig, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.GET -> !! / "quote" / "random"       =>
        val rows: Int    = ConfigHttp.getQueryParameter(req, ("rows", 1))
        val maxRows: Int = if rows >= 10 then 10 else rows
        ZIO.logInfo(s"Getting $maxRows random quote from the Postgres database!") *>
          (for
            results <- Persistence.runRandomQuote(maxRows)
            quotes  <- results
            _       <- ZIO.logInfo(s"Success on getting random quote of size $maxRows")
          yield ConfigHttp.convertToJson(quotes))
      case Method.GET -> !! / "quote" / uuid(uuid)           =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
          (for
            results <- Persistence.runSelectQuote(uuid)
            quote   <- results
            _       <- ZIO.logInfo(s"Success on getting quote with id $uuid")
          yield ConfigHttp.convertToJson(quote))
      case Method.GET -> !! / "quote" / "genre" / genre      =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with genre type $genre!") *>
          (for
            results <- Persistence.runSelectGenreQuote(genre)
            quotes  <- results
            _ <- ZIO.logInfo(s"Success on getting quote with genre $genre, size: ${quotes.size}")
          yield ConfigHttp.convertToJson(quotes))
      case Method.GET -> !! / "quote" / "genre-title" / term =>
        ZIO.logInfo(s"Getting a genre titles from the Postgres database that starts with $term!") *>
          (for
            results <- Persistence.runSelectGenreTitles(term)
            genres  <- results
            _       <- ZIO.logInfo(
              s"Success on getting genre titles starting $term, size: ${genres.size}"
            )
          yield ConfigHttp.convertToJson(genres))
    }
