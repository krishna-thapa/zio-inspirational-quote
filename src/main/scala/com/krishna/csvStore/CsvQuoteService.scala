package com.krishna.csvStore

import java.io.IOException
import java.time.LocalDate
import java.util.UUID

import scala.Option.unless

import zio.*
import zio.stream.{ ZPipeline, ZSink, ZStream }

import com.krishna.config.*
import com.krishna.database.DbConnection
import com.krishna.database.quotes.QuoteRepo
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.{ AuthorDetail, InspirationalQuote, Quote }
import com.krishna.wikiHttp.WebClient

object CsvQuoteService:

  /**
   * Convert each line from the CSV file to class object of InspirationalQuote
   * @param line
   *   String that represent each line in the CSV data store
   * @return
   *   InspirationalQuote that represents the Quote record
   */
  private def toInspirationQuote(
    line: String
  ): IO[Throwable, InspirationalQuote] =
    final case class MissingQuote(message: String) extends Throwable(message)

    val splitValue: Array[String] = line.split(";")
    val quote: String             = splitValue(0)

    val authorWithInfo: Array[String] = splitValue(1).split(",")
    val title: String                 = authorWithInfo.head.trim
    val relatedInfo: Option[String]   =
      Option(authorWithInfo.tail.mkString(", ").trim).filter(_.nonEmpty)

    for quote <- ZIO
        .fromOption(unless(quote.isEmpty)(quote))
        .orElseFail(MissingQuote(s"Quote is missing from CSV file."))
    yield InspirationalQuote(
      serialId = UUID.randomUUID(),
      quote = Quote(quote),
      author = Option.unless(title.isEmpty)(title),
      relatedInfo = relatedInfo,
      genre = splitValue(2).split(",").map(_.trim).toSet,
      storedDate = LocalDate.now()
    )

  /**
   * Collect all the Quotes as a Case class value
   */
  val collectQuotes: ZSink[Any, Nothing, InspirationalQuote, Nothing, Chunk[InspirationalQuote]] =
    ZSink.collectAll

  /**
   * Create a Stream of String lines from the selected CSV file
   */
  val csvStream: String => ZStream[Any, IOException, String] = csvPath =>
    ZStream
      .fromResource(csvPath)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)

  /**
   * Validate the number of rows that the user wants to get from the CSV file. It will be from the
   * record 0 and the default value is 20
   */
  val validateRows: Option[Int] => UIO[RuntimeFlags] = rows =>
    ZIO
      .fromOption(rows)
      .orElse(
        ZIO.logError(s"Invalid rows input $rows, selecting default value of 20") *> ZIO
          .succeed(
            20
          )
      )

  /**
   * Read the quotes from the CSV file and return the Class objects
   * @param rows
   *   Number of records to be return, each record represent the a row in CSV file
   * @return
   *   Chunk of InspirationalQuote that represents the Quote roq in CSV file
   */
  def getQuotesFromCsv(
    rows: Option[Int] = None
  ): Task[Chunk[InspirationalQuote]] =
    for
      quoteConfig <- CsvUtil.quoteConfig
      getRows     <- validateRows(rows)
      result      <- csvStream(quoteConfig.csvPath)
        .take(getRows)
        .mapZIOPar(quoteConfig.batchSize)(toInspirationQuote)
        .run(collectQuotes)
        .tapError(ex => ZIO.logError(s"Error while $ex"))
      _ <- ZIO.logInfo(s"Finishing retrieving total quote records of size: ${result.size}.")
    yield result

  /**
   * Pipeline to insert a quote to Database Postgres
   */
  private def toInspirationQuoteAndInsertToDb(
    line: String
  ): ZIO[QuoteRepo, Throwable, Unit] =
    for
      quote <- toInspirationQuote(line)
      _     <- QuoteRepo.runMigrateQuote(quote)
    yield ()

  /**
   * Collect all total Quotes count that is migrated to Database
   */

  val collectQuotesQuotes: ZSink[Any, Nothing, Any, Nothing, Long] =
    ZSink.count

  /**
   * Reads the quotes from the CSV file and store to the Postgres Database
   * @return
   *   Total amount of records stored in the Database
   */
  def migrateQuotesToDb(): ZIO[QuoteRepo, Throwable, Long] =
    for
      _           <- QuoteRepo.runTruncateTable()
      quoteConfig <- CsvUtil.quoteConfig
      result      <- csvStream(quoteConfig.csvPath)
        .drop(1) // Drop the CSV header row
        .mapZIOPar(quoteConfig.batchSize)(toInspirationQuoteAndInsertToDb)
        .run(collectQuotesQuotes)
        .tapError(ErrorHandle.handelError("migrateQuotesToDb", _))
      _           <- ZIO.logInfo(
        s"Successfully migrated total quotes $result from CSV data to Postgres Database."
      )
    yield result
