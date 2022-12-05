package com.krishna.main

import com.krishna.main.WikiApiClient.program
import com.krishna.model.AuthorDetail.isEmpty
import com.krishna.model.InspirationalQuote
import com.krishna.model.Quotes.Quote
import com.krishna.wikiHttp.{ WebClient, WikiHttpService }
import zio.logging.{ LogFilter, LogFormat, console }
import zio.stream.{ ZPipeline, ZSink, ZStream }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import java.io.IOException
import java.util.UUID

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> console(
      LogFormat.colored,
      LogFilter
        .logLevelByName(
          LogLevel.Info,
          "io.netty" -> LogLevel.Warning,
        )
        .cached,
    )

//    val splintOnQuote: ZPipeline[Any, Nothing, String, InspirationalQuote] = ZPipeline.map { line =>
//      val splitValue = line.split(";")
//      InspirationalQuote(
//        serialId = UUID.randomUUID(),
//        quote = Quote(splitValue(0)),
//        author = Option(splitValue(1)).filter(_.nonEmpty),
//        genre = splitValue(2).split(",").map(_.trim).toSet,
//      )
//    }

  def toInspirationQuote(line: String): ZIO[WebClient, Throwable, InspirationalQuote] =
    val splitValue: Array[String] = line.split(";")
    for authorDetails <- WebClient.getAuthorDetail(splitValue(1))
    yield InspirationalQuote(
      serialId = UUID.randomUUID(),
      quote = Quote(splitValue(0)),
      author = Option(authorDetails).filterNot(isEmpty),
      genre = splitValue(2).split(",").map(_.trim).toSet,
    )

  val collectQuotes: ZSink[Any, Nothing, InspirationalQuote, Nothing, Chunk[InspirationalQuote]] =
    ZSink.collectAll

  val sourceCsvFile: ZIO[WebClient, Throwable, Chunk[InspirationalQuote]] = ZStream
    .fromResource("quotes/Quotes-test.csv")
    .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
    .mapZIOPar(5)(toInspirationQuote)
    .run(collectQuotes)

  // Catch IOException && CharacterCodingException && Throwable

  val program: ZIO[WebClient, Throwable, ExitCode] =
    for
      _ <- ZIO.logInfo("Running ZIO inspirational quote project!!")
      bar <- sourceCsvFile
      _ <- ZIO.logInfo(s"Debugging: ${bar.drop(1).take(2)}")
    yield ExitCode.success

  override val run: ZIO[Any, Throwable, ExitCode] =
    program.provide(WikiHttpService.layer)
