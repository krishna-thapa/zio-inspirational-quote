package com.krishna.database.quotes

import com.krishna.config
import com.krishna.config.DatabaseConfig
import com.krishna.database.quotes.SqlQuote.*
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.{ AuthorDetail, InspirationalQuote }
import com.krishna.util.DbUtils.*
import com.krishna.util.SqlCommon.*
import com.krishna.util.{ DateConversion, RedisClient }
import com.krishna.wikiHttp.WebClient
import zio.*
import zio.json.*
import zio.stream.{ UStream, ZSink, ZStream }

import java.util.UUID
import scala.concurrent.duration.DurationInt

case class QuoteDbService() extends QuoteRepo with RedisClient:

  override val redisConfig: Task[config.RedisConfig] = getRedisConfig

  /**
   * Truncate the given table
   * @return
   *   Task that represent the SQL Truncate effect
   */
  def runTruncateTable(): Task[RuntimeFlags] =
    for
      tableName <- getQuoteTable
      response  <- runUpdateTxa(truncateTable(tableName))
    yield response

  /**
   * Insert the quote to the Postgres DB table
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
        _           <- ZIO.logInfo("Retrieving a random quote of the day from the Postgres DB!")
        randomQuote <- runRandomQuote(1)
        quote = randomQuote.head // runRandomQuote will always have at least one record, exception is handle already
        _         <- ZIO.logInfo(
          s"Checking if the retrieved quote id: ${quote.serialId} from DB is present in list cached list!"
        )
        isPresent <- isPresentInCachedQuoteIds(quote.serialId)
        _         <-
          if isPresent then
            ZIO.logInfo(
              s"Quote with id: ${quote.serialId} is already present in the cached ids list, getting a new one!"
            ) *> runQuoteOfTheDayQuote()
          else
            ZIO.logInfo(
              s"Storing id: ${quote.serialId} in the cached quote ids list!"
            ) *> setCachedQuoteIds(quote.serialId)
        _         <- setCachedQuote(quote.toJson)
      yield quote

    for
      _ <- ZIO.logInfo("Checking if the quote of the day is already defined and stored in cache")
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

  /**
   * Retrieve all the quotes from the Postgres Database
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

  /**
   * Get single or multiple random quotes
   * use of different Postgres Select query depending on the input rows for efficient query call
   * @param rows
   *   default to value 1, maximum with the value of 10
   * @return
   *   List of Random quote/s
   */
  def runRandomQuote(rows: Int): Task[List[InspirationalQuote]] =
    for
      tableName <- getQuoteTable
      response  <-
        if rows == 1 then runQueryTxa(getOneRandomQuote(tableName))
        else runQueryTxa(getRandomQuotes(tableName, rows))
    yield response.toList

  /**
   * Get a quote by its UUID
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

  /**
   * Insert the favourite quote to the database with user id, if the quote is already exist then
   * toggle the boolean tag for the quote id.
   * @param userId Id that represents the User from the user details table
   * @param quoteId Id that represent the quote
   * @return Response from the Update query in the table
   */
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

  /**
   * Get all the favourite quotes from the table for a user
   * @param userId Id that represents the User from the user details table
   * @param historyQuotes Weather to include all the quotes that the user had marked as favorites in past
   * @return List of the quotes
   */
  def runGetAllFavQuotes(userId: UUID, historyQuotes: Boolean): Task[List[InspirationalQuote]] =
    for
      quoteTable   <- getQuoteTable
      userFavTable <- getFavTable
      favQuotes    <- runQueryTxa(getAllFavQuotes(quoteTable, userFavTable, userId, historyQuotes))
    yield favQuotes

  /**
   * Get a Quote by its genre
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

  /**
   * Full text search using the Postgres TS vector, more in this article:
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

  /**
   * Auto-completed logic on selecting the genre that is present in any quotes
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

  // ============================= Author Detail Table =====================

  private def runUploadAuthorDetail(authorDetail: AuthorDetail, authorTable: String): Task[Int] =
    for response <- runUpdateTxa(insertAuthor(authorTable, authorDetail))
    yield response

  /**
   * Get all the distinct authors from the Postgres table
   * @return
   *   Authors
   */
  def runGetAndUploadAuthorDetails(): ZIO[WebClient, Throwable, Long] =
    for
      authorTable    <- getAuthorTable
      _              <- runUpdateTxa(truncateTable(authorTable))
      tableName      <- getQuoteTable
      authors        <- runQueryTxa(getAuthors(tableName))
      insertResponse <-
        ZStream
          .fromChunk(Chunk.fromIterable(authors))
          .mapZIOPar(5) { author =>
            for
              authorDetail <- WebClient.getAuthorDetail(author)
              response     <- runUploadAuthorDetail(authorDetail, authorTable)
            yield response
          }
          .run(ZSink.count)
          .tapError(ErrorHandle.handelError("runGetAllAuthors", _))
    yield insertResponse

  def runGetAuthorDetail(author: String): Task[Option[AuthorDetail]] =
    for
      tableName <- getAuthorTable
      author    <- runQueryTxa(getAuthor(tableName, author))
    yield author

object QuoteDbService:
  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
