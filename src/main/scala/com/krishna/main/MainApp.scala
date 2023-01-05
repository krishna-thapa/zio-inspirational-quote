package com.krishna.main

import java.io.IOException

import zio.config.ReadError
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*
import zio.logging.backend.SLF4J
import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import com.krishna.config.*
import com.krishna.database.quotes.{ Persistence, QuoteDbService }
import com.krishna.database.{ DatabaseMigrator, DbConnection }
import com.krishna.http.{ AdminHttp, HomePage }
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.CsvQuoteService

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val port: Int = 9000

  private val combinedHttp: Http[Persistence with QuoteAndDbConfig, Throwable, Request, Response] =
    HomePage() ++ AdminHttp()

  val config: ServerConfig = ServerConfig
    .default
    .port(port)
    .leakDetection(LeakDetectionLevel.PARANOID)
    .maxThreads(1)

  val configLayer: ULayer[ServerConfig] = ServerConfig.live(config)

//  val program: ZIO[Persistence with Configuration, Throwable, Unit] =
//    for
//      _    <- ZIO.logInfo("Running ZIO inspirational quote API project!!")
//      port <- Server.install(combinedHttp)
//      _    <- ZIO.logInfo(s"Starting server on http://localhost:$port")
//      _    <- DatabaseMigrator.migrate // <*> Server.install(combinedHttp)
//    yield ()

  // =========================================
  def logAndFail(errorMsg: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    ZIO.logError(errorMsg) *> ZIO.fail(exception)

  // Catch IOException && CharacterCodingException && Throwable
  def errorHandler(exception: Throwable): ZIO[Any, Throwable, Unit] =
    exception match
      case ex: IOException => logAndFail("Failed while reading the CSV file!", ex)
      case ex              => logAndFail("Generic fail", ex)
  // ===========================================

  private val environmentLayers =
    configLayer >+> Server.live >+> Configuration.layer >+> QuoteDbService.layer

  override val run: ZIO[Environment & ZIOAppArgs, Any, Any] =
    (DatabaseMigrator.migrate <*> Server.install(combinedHttp).flatMap { port =>
      Console.printLine(s"Started server on port: $port")
    } *> ZIO.never)
      .provide(configLayer, Server.live, Configuration.layer, QuoteDbService.layer)
      .map(_ => ExitCode.success)

//    DatabaseMigrator.migrate <*> Server.install(combinedHttp)
//      .provide(environmentLayers)
//      .catchAll(errorHandler)
//      .map(_ => ExitCode.success)

//    for
//      //      layers <- environmentLayers
//      _ <- program
//        .provideLayer(Configuration.layer)
//        .catchAll(errorHandler)
//        .map(_ => ExitCode.success)
//    yield ()
