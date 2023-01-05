package com.krishna.database.quotes

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.InspirationalQuote

trait Persistence:

  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]
  def runMigrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]

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
