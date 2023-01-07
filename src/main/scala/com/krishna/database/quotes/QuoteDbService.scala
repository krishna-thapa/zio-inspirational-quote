package com.krishna.database.quotes

import java.util.UUID

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote
import com.krishna.util.DbUtils.validateDbTable
import com.krishna.util.sqlCommon.*

case class QuoteDbService() extends QuoteRepo:

  /** Truncate the given table
    * @return
    *   Task that represent the SQL Truncate effect
    */
  def runTruncateTable(): Task[RuntimeFlags] =
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
  ): Task[RuntimeFlags] =
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
  ): Task[List[InspirationalQuote]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getAllQuotes(tableName, offset, limit))
    yield response

  def runRandomQuote(rows: Int): Task[List[InspirationalQuote]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getRandomQuote(tableName, rows))
    yield response

  def runSelectQuote(uuid: UUID): Task[InspirationalQuote] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getQuoteById(tableName, uuid))
    yield response

  def runSelectGenreQuote(
    genre: String
  ): Task[List[InspirationalQuote]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getQuoteByGenre(tableName, genre))
    yield response.toList

  def runSelectGenreTitles(term: String): Task[List[String]] =
    for
      tableName <- validateDbTable
      response  <- runQueryTxa(getGenreTitles(tableName, term))
    yield response.flatten

object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
