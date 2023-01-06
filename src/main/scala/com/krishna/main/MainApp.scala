package com.krishna.main

import java.io.IOException

import zio.config.ReadError
import zio.http.ServerConfig.LeakDetectionLevel
import zio.http.*
import zio.logging.backend.SLF4J
import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import com.krishna.config.*
import com.krishna.csvStore.CsvQuoteService
import com.krishna.database.quotes.{ Persistence, QuoteDbService }
import com.krishna.database.{ DatabaseMigrator, DbConnection }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.http.ConfigHttp
import com.krishna.http.api.{ AdminHttp, HomePage }
import com.krishna.model.InspirationalQuote

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val program =
    for
      _    <- ZIO.logInfo("Running ZIO inspirational quote API project!!")
      port <- DatabaseMigrator.migrate <*> Server.install(ConfigHttp.combinedHttp)
      _    <- ZIO.logInfo(s"Starting server on http://localhost:$port")
      _    <- ZIO.never
    yield ()

//  private val environmentLayers =
//    List(configLayer, Server.live, Configuration.layer, QuoteDbService.layer)

  override val run: ZIO[Environment & ZIOAppArgs, Any, Any] =
    program
      .provide(ConfigHttp.configLayer, Server.live, Configuration.layer, QuoteDbService.layer)
      .catchAll(ErrorHandle.handelError("main app", _))
      .map(_ => ExitCode.success)
