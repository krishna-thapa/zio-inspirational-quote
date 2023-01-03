package com.krishna.main

import java.io.IOException

import zhttp.http.*
import zhttp.service.Server
import zio.config.ReadError
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

  val program: ZIO[Persistence with Configuration, Throwable, Unit] =
    for
      _ <- ZIO.logInfo("Running ZIO inspirational quote API project!!")
      _ <- ZIO.logInfo(s"Starting server on http://localhost:$port")
      _ <- DatabaseMigrator.migrate <*> Server.start(port, combinedHttp)
    yield ()

  // =========================================
  def logAndFail(errorMsg: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    ZIO.logError(errorMsg) *> ZIO.fail(exception)

  // Catch IOException && CharacterCodingException && Throwable
  def errorHandler(exception: Throwable): ZIO[Any, Throwable, Unit] =
    exception match
      case ex: IOException => logAndFail("Failed while reading the CSV file!", ex)
      case ex              => logAndFail("Generic fail", ex)
  // ===========================================

  private val environmentLayers = Configuration.layer >+> QuoteDbService.layer

  override val run: ZIO[Environment & ZIOAppArgs, Any, Any] =
    program
      .provide(environmentLayers)
      .catchAll(errorHandler)
      .map(_ => ExitCode.success)

//    for
//      //      layers <- environmentLayers
//      _ <- program
//        .provideLayer(Configuration.layer)
//        .catchAll(errorHandler)
//        .map(_ => ExitCode.success)
//    yield ()
