package com.krishna.main

import zio.*
import zio.http.*
import zio.logging.backend.SLF4J

import com.krishna.config.*
import com.krishna.database.DatabaseMigrator
import com.krishna.database.quotes.QuoteDbService
import com.krishna.database.user.UserDbService
import com.krishna.errorHandle.ErrorHandle
import com.krishna.http.ConfigHttp
import com.krishna.wikiHttp.WikiHttpService

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val program =
    for
      _    <- ZIO.logInfo("Running ZIO inspirational quote API project!")
      port <- DatabaseMigrator.migrate <*> Server.install(
        ConfigHttp.httpsWithMiddlewares.withDefaultErrorResponse
      )
      _    <- ZIO.logInfo(s"Starting server on http://localhost:$port")
      // _    <- QuoteOfTheDayScheduler.getQuoteOfTheDay
      _    <- ZIO.never
    yield ()

  override val run: ZIO[Environment & ZIOAppArgs, Any, Any] =
    program
      .provide(
        ConfigHttp.configLayer,
        Server.live,
        QuoteDbService.layer,
        UserDbService.layer,
        WikiHttpService.layer
        // EmailService.layer
      )
      .catchAll(ErrorHandle.handelError("main app", _))
      .exitCode
