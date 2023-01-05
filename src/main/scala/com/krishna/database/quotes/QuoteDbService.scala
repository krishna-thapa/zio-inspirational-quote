package com.krishna.database.quotes

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote

case class QuoteDbService() extends Persistence:

  /** Validates the table name that is coming from the config file
    */
  private val validateDbTable: ZIO[DatabaseConfig, RuntimeException, String] =
    for
      getDbConfig <- com.krishna.config.databaseConfig
      tableName   <- DatabaseConfig.validateTable(getDbConfig)
    yield tableName

  /** Truncate the given table
    * @return
    *   Task that represent the SQL Truncate effect
    */
  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    for
      tableName <- validateDbTable
      response  <- runUpdateTxa(truncateTable(tableName))
    yield response

  /** Insert the quote to the Postgres DB table
    * @param quote
    *   InspirationalQuote to be inserted
    * @return
    *   Task that represent the SQL Insert effect
    */
  def runMigrateQuote(
    quote: InspirationalQuote
  ): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    for
      tableName <- validateDbTable
      response  <- runUpdateTxa(insertQuote(tableName, quote))
    yield response

  /**
   * Retrieve all the quotes from the Postgres Database
   * @return List of InspirationalQuote
   */
  def runGetAllQuotes(): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getAllQuotes(tableName))
    yield response

object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
