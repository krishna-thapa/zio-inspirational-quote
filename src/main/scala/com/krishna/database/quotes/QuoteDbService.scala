package com.krishna.database.quotes

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote

case class QuoteDbService() extends Persistence:

  private val validateDbTable: ZIO[DatabaseConfig, RuntimeException, String] =
    for
      getDbConfig <- com.krishna.config.databaseConfig
      tableName   <- DatabaseConfig.validateTable(getDbConfig)
    yield tableName

  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    for
      tableName <- validateDbTable
      response  <- runDoobieTxa(truncateTable(tableName))
    yield response

  def runMigrateQuote(
    quote: InspirationalQuote
  ): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    for
      tableName <- validateDbTable
      response  <- runDoobieTxa(insertQuote(tableName, quote))
    yield response

object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
