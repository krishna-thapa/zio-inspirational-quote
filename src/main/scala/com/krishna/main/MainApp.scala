package com.krishna.main

import com.krishna.config.EnvironmentConfig
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.ReadQuoteCsv
import com.krishna.wikiHttp.{ WebClient, WikiHttpService }
import zio.logging.{ LogFilter, LogFormat, console }
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

  // Catch IOException && CharacterCodingException && Throwable

  val program: ZIO[WebClient with EnvironmentConfig, Throwable, ExitCode] =
    for
      _ <- ZIO.logInfo("Running ZIO inspirational quote project!!")
      quotes <- ReadQuoteCsv.getQuotesFromCsv
      bar = quotes.filter(_.author.exists(_.imagerUrl.isEmpty))
      _ <- ZIO.logInfo(s"Debugging: ${quotes.drop(1).take(2)}")
    yield ExitCode.success

  override val run: ZIO[Any, Throwable, ExitCode] =
    program.provide(WikiHttpService.layer, EnvironmentConfig.layer)
