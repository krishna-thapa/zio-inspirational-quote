package com.krishna.database.quotes

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.model.InspirationalQuote

trait Persistence:
  def migrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]]

object Persistence:

  def migrateQuote(
    quote: InspirationalQuote
  ): ZIO[Persistence with DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    ZIO.serviceWithZIO[Persistence](_.migrateQuote(quote))
