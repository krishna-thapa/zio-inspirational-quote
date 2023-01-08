package com.krishna.main

import zio.http.*
import zio.logging.backend.SLF4J
import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import com.krishna.config.*
import com.krishna.database.DatabaseMigrator
import com.krishna.database.quotes.QuoteDbService
import com.krishna.database.user.UserDbService
import com.krishna.errorHandle.ErrorHandle
import com.krishna.http.ConfigHttp

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val program =
    for
      _    <- ZIO.logInfo("Running ZIO inspirational quote API project!!")
      port <- DatabaseMigrator.migrate <*> Server.install(ConfigHttp.combinedHttps)
      _    <- ZIO.logInfo(s"Starting server on http://localhost:$port")
      _    <- ZIO.never
    yield ()

//  private val environmentLayers =
//    List(configLayer, Server.live, Configuration.layer, QuoteDbService.layer)

  override val run: ZIO[Environment & ZIOAppArgs, Any, Any] =
    program
      .provide(
        ConfigHttp.configLayer,
        Server.live,
        QuoteDbService.layer,
        UserDbService.layer
      )
      .catchAll(ErrorHandle.handelError("main app", _))
      .map(_ => ExitCode.success)
