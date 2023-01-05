package com.krishna.database.quotes

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.model.InspirationalQuote

trait Persistence:

  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]
  def runMigrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]

object Persistence:

  def runTruncateTable(): ZIO[Persistence with DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    ZIO.serviceWithZIO[Persistence](_.runTruncateTable())

  def runMigrateQuote(
    quote: InspirationalQuote
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    ZIO.serviceWithZIO[Persistence](_.runMigrateQuote(quote))
