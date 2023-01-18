package com.krishna.database.quotes

import java.util.UUID

import zio.*

import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote
import com.krishna.util.DbUtils.getQuoteTable
import com.krishna.util.sqlCommon.*

case class QuoteDbService() extends QuoteRepo:

  /** Truncate the given table
    * @return
    *   Task that represent the SQL Truncate effect
    */
  def runTruncateTable(): Task[RuntimeFlags] =
    for
      tableName <- getQuoteTable
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
      tableName <- getQuoteTable
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
      tableName <- getQuoteTable
      response  <- runQueryTxa(getAllQuotes(tableName, offset, limit))
    yield response

  /** Get single or multiple random quote
    * @param rows
    *   default o value 0
    * @return
    *   List of Random quote/s
    */
  def runRandomQuote(rows: Int): Task[List[InspirationalQuote]] =
    for
      tableName <- getQuoteTable
      response  <- runQueryTxa(getRandomQuote(tableName, rows))
    yield response

  /** Get a quote by its UUID
    * @param uuid
    *   selected uuid
    * @return
    *   Selected quote
    */
  def runSelectQuote(uuid: UUID): Task[InspirationalQuote] =
    for
      tableName <- getQuoteTable
      response  <- runQueryTxa(getQuoteById(tableName, uuid))
    yield response

  /** Get a Quote by its genre
    * @param genre
    *   to be selected from any quote
    * @return
    *   Random 5 quotes from the selected genre
    */
  def runSelectGenreQuote(
    genre: String
  ): Task[List[InspirationalQuote]] =
    for
      tableName <- getQuoteTable
      response  <- runQueryTxa(getQuoteByGenre(tableName, genre))
    yield response.toList

  /** Auto-completed logic on selecting the genre that is present int he any quotes
    * @param term
    *   User's input term should be max of three characters
    * @return
    *   List of matched genre to show the auto-suggestion while user is typing
    */
  def runSelectGenreTitles(term: String): Task[List[String]] =
    for
      tableName <- getQuoteTable
      response  <- runQueryTxa(getGenreTitles(tableName, term))
    yield response.flatten
  
object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
