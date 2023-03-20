package com.krishna.database.quotes

import java.util.UUID

import zio.*
import zio.stream.UStream

import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.InspirationalQuote
import com.krishna.wikiHttp.WebClient

trait QuoteRepo:

  def runTruncateTable(): Task[RuntimeFlags]

  def runMigrateQuote(quote: InspirationalQuote): Task[RuntimeFlags]

  def runQuoteOfTheDayQuote(): Task[InspirationalQuote]

  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): Task[List[InspirationalQuote]]

  def runRandomQuote(rows: Int): Task[List[InspirationalQuote]]

  def runSelectQuote(uuid: UUID): Task[InspirationalQuote]

  def runUpdateFavQuote(
    userId: UUID,
    quoteId: String
  ): Task[Int]

  def runGetAllFavQuotes(userId: UUID, historyQuotes: Boolean): Task[List[InspirationalQuote]]

  def runSelectGenreQuote(
    genre: String
  ): Task[List[InspirationalQuote]]

  def runSearchQuote(
    searchInput: String
  ): Task[List[InspirationalQuote]]

  def runSelectGenreTitles(term: String): Task[List[String]]

  def runGetAndUploadAuthorDetails(): ZIO[WebClient, Throwable, Long]

object QuoteRepo:

  def runTruncateTable(): ZIO[QuoteRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runTruncateTable().foldZIO(
        err => ErrorHandle.handelError("runTruncateTable", err),
        _ => ZIO.logInfo("Success on truncating the table inspiration_quote_db.")
      )
    )

  def runMigrateQuote(
    quote: InspirationalQuote
  ): ZIO[QuoteRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runMigrateQuote(quote).foldZIO(
        err => ErrorHandle.handelError("runMigrateQuote", err),
        _ => ZIO.logInfo(s"Success on inserting quote with uuid: ${quote.serialId}")
      )
    )

  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runGetAllQuotes(offset, limit).tapError(ex =>
        ZIO.logError(s"Error while running runGetAllQuotes, with exception:  $ex")
      )
    )

  def runQuoteOfTheDayQuote(): ZIO[QuoteRepo, Throwable, InspirationalQuote] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runQuoteOfTheDayQuote().tapError(ex =>
        ZIO.logError(s"Error while running runQuoteOfTheDayQuote, with exception:  $ex")
      )
    )

  def runRandomQuote(
    rows: Int
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runRandomQuote(rows).tapError(ex =>
        ZIO.logError(s"Error while running runRandomQuote, with exception:  $ex")
      )
    )

  def runSelectQuote(
    uuid: UUID
  ): ZIO[QuoteRepo, Throwable, InspirationalQuote] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runSelectQuote(uuid).tapError(ex =>
        ZIO.logError(s"Error while running runSelectQuote, with exception:  $ex")
      )
    )

  def runUpdateFavQuote(
    userId: UUID,
    quoteId: String
  ): ZIO[QuoteRepo, Throwable, RuntimeFlags] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runUpdateFavQuote(userId, quoteId).tapError(ex =>
        ZIO.logError(s"Error while running runUpdateFavQuote, with exception:  $ex")
      )
    )

  def runGetAllFavQuotes(
    userId: UUID,
    historyQuotes: Boolean
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runGetAllFavQuotes(userId, historyQuotes).tapError(ex =>
        ZIO.logError(s"Error while running runGetAllFavQuotes, with exception:  $ex")
      )
    )

  def runSelectGenreQuote(
    genre: String
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runSelectGenreQuote(genre).tapError(ex =>
        ZIO.logError(s"Error while running runSelectGenreQuote, with exception:  $ex")
      )
    )

  def runSearchQuote(
    searchInput: String
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runSearchQuote(searchInput).tapError(ex =>
        ZIO.logError(s"Error while running runSearchQuote, with exception:  $ex")
      )
    )

  def runSelectGenreTitles(
    term: String
  ): ZIO[QuoteRepo, Throwable, List[String]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runSelectGenreTitles(term).tapError(ex =>
        ZIO.logError(s"Error while running runSelectGenreTitles, with exception:  $ex")
      )
    )

  def runGetAndUploadAuthorDetails(): ZIO[QuoteRepo with WebClient, Throwable, Long] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runGetAndUploadAuthorDetails().tapError(ex =>
        ZIO.logError(s"Error while running runGetAndUploadAuthorDetails, with exception:  $ex")
      )
    )
