package com.krishna.database.quotes

import zio.*
import com.krishna.config.DatabaseConfig
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.InspirationalQuote

import java.util.UUID

trait Persistence:

  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]
  def runMigrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]

  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]]

  def runRandomQuote(rows: Int): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]]

  def runSelectQuote(uuid: UUID): ZIO[DatabaseConfig, Throwable, Task[InspirationalQuote]]

  def runSelectGenreQuote(
    genre: String
  ): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]]

  def runSelectGenreTitles(term: String): ZIO[DatabaseConfig, Throwable, Task[List[String]]]

object Persistence:

  def runTruncateTable(): ZIO[Persistence with DatabaseConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[Persistence](
      _.runTruncateTable().foldZIO(
        err => ErrorHandle.handelError("runTruncateTable", err),
        _ => ZIO.logInfo("Success on truncating the table inspiration_quote_db!")
      )
    )

  def runMigrateQuote(
    quote: InspirationalQuote
  ): ZIO[Persistence with DatabaseConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[Persistence](
      _.runMigrateQuote(quote).foldZIO(
        err => ErrorHandle.handelError("runMigrateQuote", err),
        _ => ZIO.logInfo(s"Success on inserting quote with uuid: ${quote.serialId}")
      )
    )

  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    ZIO.serviceWithZIO[Persistence](
      _.runGetAllQuotes(offset, limit).tapError(ex =>
        ZIO.logError(s"Error while running runGetAllQuotes, with exception:  $ex")
      )
    )

  def runRandomQuote(
    rows: Int
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    ZIO.serviceWithZIO[Persistence](
      _.runRandomQuote(rows).tapError(ex =>
        ZIO.logError(s"Error while running runRandomQuote, with exception:  $ex")
      )
    )

  def runSelectQuote(
    uuid: UUID
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[InspirationalQuote]] =
    ZIO.serviceWithZIO[Persistence](
      _.runSelectQuote(uuid).tapError(ex =>
        ZIO.logError(s"Error while running runSelectQuote, with exception:  $ex")
      )
    )

  def runSelectGenreQuote(
    genre: String
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    ZIO.serviceWithZIO[Persistence](
      _.runSelectGenreQuote(genre).tapError(ex =>
        ZIO.logError(s"Error while running runSelectGenreQuote, with exception:  $ex")
      )
    )

  def runSelectGenreTitles(
    term: String
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[List[String]]] =
    ZIO.serviceWithZIO[Persistence](
      _.runSelectGenreTitles(term).tapError(ex =>
        ZIO.logError(s"Error while running runSelectGenreTitles, with exception:  $ex")
      )
    )
