package com.krishna.readCsv

import java.io.IOException
import java.util.UUID

import zio.stream.{ ZPipeline, ZSink, ZStream }
import zio.{ Chunk, ZIO }

import com.krishna.config.EnvironmentConfig
import com.krishna.model.Quotes.Quote
import com.krishna.model.{ AuthorDetail, InspirationalQuote }
import com.krishna.wikiHttp.WebClient

object ReadQuoteCsv:

  private def toInspirationQuote(
    line: String): ZIO[WebClient with EnvironmentConfig, Throwable, InspirationalQuote] =
    val splitValue: Array[String] = line.split(";")
    for authorDetails <- WebClient.getAuthorDetail(splitValue(1))
    yield InspirationalQuote(
      serialId = UUID.randomUUID(),
      quote = Quote(splitValue(0)),
      author = Option(authorDetails).filterNot(AuthorDetail.isEmpty),
      genre = splitValue(2).split(",").map(_.trim).toSet
    )

  private val collectQuotes
    : ZSink[Any, Nothing, InspirationalQuote, Nothing, Chunk[InspirationalQuote]] =
    ZSink.collectAll

  def getQuotesFromCsv
    : ZIO[WebClient with EnvironmentConfig, Throwable, Chunk[InspirationalQuote]] =
    for
      environmentConfig <- ZIO.service[EnvironmentConfig]
      result <- ZStream
        .fromResource(environmentConfig.csvPath)
        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
        .mapZIOPar(environmentConfig.batchSize)(toInspirationQuote)
        .run(collectQuotes)
    yield result
