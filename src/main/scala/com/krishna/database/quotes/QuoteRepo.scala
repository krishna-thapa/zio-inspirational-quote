package com.krishna.database.quotes

import java.util.UUID

import zio.*

import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.InspirationalQuote

trait QuoteRepo:

  def runTruncateTable(): Task[RuntimeFlags]
  def runMigrateQuote(quote: InspirationalQuote): Task[RuntimeFlags]

  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): Task[List[InspirationalQuote]]

  def runRandomQuote(rows: Int): Task[List[InspirationalQuote]]

  def runSelectQuote(uuid: UUID): Task[InspirationalQuote]

  def runSelectGenreQuote(
    genre: String
  ): Task[List[InspirationalQuote]]

  def runSelectGenreTitles(term: String): Task[List[String]]

object QuoteRepo:

  def runTruncateTable(): ZIO[QuoteRepo, Throwable, Unit] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runTruncateTable().foldZIO(
        err => ErrorHandle.handelError("runTruncateTable", err),
        _ => ZIO.logInfo("Success on truncating the table inspiration_quote_db!")
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

  def runSelectGenreQuote(
    genre: String
  ): ZIO[QuoteRepo, Throwable, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[QuoteRepo](
      _.runSelectGenreQuote(genre).tapError(ex =>
        ZIO.logError(s"Error while running runSelectGenreQuote, with exception:  $ex")
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
