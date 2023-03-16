package com.krishna.database.quotes

import java.util.UUID

import scala.concurrent.duration.DurationInt

import zio.*
import zio.json.*

import com.krishna.config
import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.model.InspirationalQuote
import com.krishna.util.DbUtils.*
import com.krishna.util.SqlCommon.*
import com.krishna.util.{ DateConversion, RedisClient }

case class QuoteDbService() extends QuoteRepo with RedisClient:

  override val redisConfig: Task[config.RedisConfig] = getRedisConfig

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

  def runQuoteOfTheDayQuote(): Task[InspirationalQuote] =
    lazy val getRandomQuoteFromDb: ZIO[Any, Throwable, InspirationalQuote] =
      for
        _           <- ZIO.logInfo("Retrieving the quote of the day from the Postgres DB!")
        randomQuote <- runRandomQuote(1)
        _           <- setCachedQuote(randomQuote.head.toJson)
      yield randomQuote.head

    for
      _           <- ZIO.logInfo("Checking if the quote of the day is already defined")
      optionQuote <- getCachedQuote
      nextQuote   <-
        if optionQuote.isDefined then
          optionQuote.get.fromJson[InspirationalQuote] match
            case Left(errorMsg) => ZIO.fail(new Exception(s"Parsing error with message: $errorMsg"))
            case Right(quote)   =>
              ZIO.logInfo("Quote is found in the Redis cache memory!") *> ZIO.succeed(
                quote
              )
        else getRandomQuoteFromDb
    yield nextQuote

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
    yield response.toList

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

  def runUpdateFavQuote(
    userId: UUID,
    quoteId: String
  ): Task[Int] =
    for
      userFavTable  <- getFavTable
      isFavRowExist <- runQueryTxa(isFavRecordExist(userFavTable, userId, quoteId))
      response      <-
        if isFavRowExist.isEmpty then
          ZIO.logInfo(s"Inserting a new fav quote for the user with id: $userId") *>
            runUpdateTxa(insertFavQuoteRow(userFavTable, userId, quoteId))
        else
          ZIO.logInfo(
            s"Quote is already in the database, toggling the fav boolean tag for quote id: $quoteId"
          ) *>
            runUpdateTxa(alterFavQuoteRow(userFavTable, isFavRowExist.get.id))
    yield response

  def runGetAllFavQuotes(userId: UUID, historyQuotes: Boolean): Task[List[InspirationalQuote]] =
    for
      quoteTable   <- getQuoteTable
      userFavTable <- getFavTable
      favQuotes    <- runQueryTxa(getAllFavQuotes(quoteTable, userFavTable, userId, historyQuotes))
    yield favQuotes

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

  /** Full text search using the Postgres TS vector, more in this article:
    * https://leandronsp.com/a-powerful-full-text-search-in-postgresql-in-less-than-20-lines
    * @param searchInput
    *   user's searched text parameter
    * @return
    *   list of matched quotes
    */
  def runSearchQuote(
    searchInput: String
  ): Task[List[InspirationalQuote]] =
    val tsQueryInput = searchInput.trim.replace("%20", "&")
    for
      tableName <- getQuoteTable
      response  <- runQueryTxa(getQuoteBySearchedText(tableName, tsQueryInput))
    yield response

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
