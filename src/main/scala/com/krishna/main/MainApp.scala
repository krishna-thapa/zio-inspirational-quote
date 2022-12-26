package com.krishna.main

import java.io.IOException
import java.util.UUID
import zhttp.http.*
import zhttp.service.Server
import zio.logging.backend.SLF4J
import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }
import com.krishna.config.*
import com.krishna.database.{ DatabaseMigrator, DbConnection }
import com.krishna.http.{ AdminHttp, HomePage }
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.CsvQuoteService
import com.krishna.wikiHttp.{ WebClient, WikiHttpService }
import zio.jdbc.ZConnectionPool

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val port: Int = 9000

  val combinedHttp: Http[QuoteConfig & ZConnectionPool, Throwable, Request, Response] =
    HomePage() ++ AdminHttp()

  val program: ZIO[Configuration & ZConnectionPool, Throwable, Unit] =
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

  val appEnvironment = Configuration.layer >+> WikiHttpService.layer
 // val foo = DbConnection.createZIOPoolConfig >>> DbConnection.dbPool

  override val run: ZIO[Environment & (ZIOAppArgs & Scope), Any, Any] =
    for {
      foo <- DbConnection.dbPool.provideLayer(Configuration.dbLayer)
      bar = DbConnection.createZIOPoolConfig >>> foo
      _ <- program.provideLayer(bar ++ appEnvironment)
    } yield ()
//    program
//      .provideLayer(appEnvironment)
//      .catchAll(errorHandler)
//      .map(_ => ExitCode.success)
