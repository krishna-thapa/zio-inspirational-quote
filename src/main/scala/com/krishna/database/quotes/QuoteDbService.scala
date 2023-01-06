package com.krishna.database.quotes

import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote
import zio.*

import java.util.UUID

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

  /** Retrieve all the quotes from the Postgres Database
    * @param offset
    *   offset value, default to 0
    * @param limit
    *   limit value, default to 10
    * @return
    *   list of InspirationalQuote
    */
  def runGetAllQuotes(
    offset: Int,
    limit: Int
  ): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getAllQuotes(tableName, offset, limit))
    yield response

  def runRandomQuote(rows: Int): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getRandomQuote(tableName, rows))
    yield response

  def runSelectQuote(uuid: UUID): ZIO[DatabaseConfig, Throwable, Task[InspirationalQuote]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getQuoteById(tableName, uuid))
    yield response

  def runSelectGenreQuote(
    genre: String
  ): ZIO[DatabaseConfig, Throwable, Task[List[InspirationalQuote]]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getQuoteByGenre(tableName, genre))
    yield response.map(_.toList)

  def runSelectGenreTitles(term: String): ZIO[DatabaseConfig, Throwable, Task[List[String]]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getGenreTitles(tableName, term))
    yield response.map(_.flatten)

object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
