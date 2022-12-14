package com.krishna.readCsv

import java.io.IOException
import java.time.LocalDate
import java.util.UUID

import scala.Option.unless

import zio.stream.{ ZPipeline, ZSink, ZStream }
import zio.{ Chunk, ZIO }

import com.krishna.configuration.*
import com.krishna.model.{ AuthorDetail, InspirationalQuote, Quote }
import com.krishna.wikiHttp.WebClient

object ReadQuoteCsv:

  private def toInspirationQuote(
    line: String
  ): ZIO[WebClient with WikiConfig, Throwable, InspirationalQuote] =
    final case class MissingQuote(message: String) extends Throwable(message)

    val splitValue: Array[String] = line.split(";")
    val quote: String             = splitValue(0)
    for
      quote         <- ZIO
        .fromOption(unless(quote.isEmpty)(quote))
        .orElseFail(MissingQuote(s"Quote is missing from CSV file."))
      authorDetails <- WebClient.getAuthorDetail(splitValue(1))
    yield InspirationalQuote(
      serialId = UUID.randomUUID(),
      quote = Quote(quote),
      author = Option(authorDetails).filterNot(AuthorDetail.isEmpty),
      genre = splitValue(2).split(",").map(_.trim).toSet,
      storedDate = LocalDate.now()
    )

  private val collectQuotes
    : ZSink[Any, Nothing, InspirationalQuote, Nothing, Chunk[InspirationalQuote]] =
    ZSink.collectAll

  def getQuotesFromCsv
    : ZIO[WebClient with QuoteAndWikiConfig, Throwable, Chunk[InspirationalQuote]] =
    for
      quoteConfig <- com.krishna.configuration.quoteConfig
      result      <- ZStream
        .fromResource(quoteConfig.csvPath)
        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
        .mapZIOPar(quoteConfig.batchSize)(toInspirationQuote)
        .run(collectQuotes)
        .tapError(ex => ZIO.logError(s"Error while $ex"))
      _ <- ZIO.logInfo(s"Finishing retrieving total quote records of size: ${result.size}.")
    yield result
