package com.krishna.readCsv

import com.krishna.config.*
import com.krishna.model.{ AuthorDetail, InspirationalQuote, Quote }
import com.krishna.wikiHttp.WebClient
import zio.*
import zio.stream.{ ZPipeline, ZSink, ZStream }

import java.io.IOException
import java.time.LocalDate
import java.util.UUID
import scala.Option.unless

object ReadQuoteCsv:

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

  private val collectQuotes
    : ZSink[Any, Nothing, InspirationalQuote, Nothing, Chunk[InspirationalQuote]] =
    ZSink.collectAll

  private val csvStream: String => ZStream[Any, IOException, String] = csvPath =>
    ZStream
      .fromResource(csvPath)
      .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)

  private val validateRows: (Option[RuntimeFlags], Boolean) => ZIO[Any, Nothing, RuntimeFlags] =
    (rows, isMigrateAll) =>
      if !isMigrateAll then
        ZIO
          .fromOption(rows)
          .orElse(
            ZIO.logError(s"Invalid rows input $rows, selecting default value of 20") *> ZIO
              .succeed(
                20
              )
          )
      else ZIO.succeed(0)

  def getQuotesFromCsv(
    rows: Option[Int] = None,
    isMigrateAll: Boolean = false
  ): ZIO[QuoteConfig, Throwable, Chunk[InspirationalQuote]] =
    for
      quoteConfig <- com.krishna.config.quoteConfig
      getRows     <- validateRows(rows, isMigrateAll)
      getCsvStream =
        if isMigrateAll then csvStream(quoteConfig.csvPath)
        else csvStream(quoteConfig.csvPath).take(getRows)
      result <- getCsvStream
        .mapZIOPar(quoteConfig.batchSize)(toInspirationQuote)
        .run(collectQuotes)
        .tapError(ex => ZIO.logError(s"Error while $ex"))
      _      <- ZIO.logInfo(s"Finishing retrieving total quote records of size: ${result.size}.")
    yield result
