package com.krishna.main

import java.io.IOException
import java.util.UUID

import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import com.krishna.config.EnvironmentConfig
import com.krishna.model.InspirationalQuote
import com.krishna.readCsv.ReadQuoteCsv
import com.krishna.wikiHttp.{ WebClient, WikiHttpService }

object MainApp extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> console(
      LogFormat.colored,
      LogFilter
        .logLevelByName(
          LogLevel.Info,
          "io.netty" -> LogLevel.Warning
        )
        .cached
    )

  // Catch IOException && CharacterCodingException && Throwable

  val program: ZIO[WebClient with EnvironmentConfig, Throwable, ExitCode] =
    for
      _      <- ZIO.logInfo("Running ZIO inspirational quote project!!")
      quotes <- ReadQuoteCsv.getQuotesFromCsv
      // bar = quotes.filter(_.author.exists(_.imagerUrl.isEmpty))
      _      <- ZIO.logInfo(s"Debugging: ${quotes.drop(1).take(2)}")
      _      <- ZIO.logInfo("Finished running ZIO application for the inspirational quote project!")
    yield ExitCode.success

  // =========================================
  def logAndFail(errorMsg: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    ZIO.logError(errorMsg) *> ZIO.fail(exception)

  def errorHandler(exception: Throwable, service: String): ZIO[Any, Throwable, Unit] = ???

  override val run: ZIO[Any, Throwable, ExitCode] =
    program
      .provide(WikiHttpService.layer, EnvironmentConfig.layer)
      .catchAll { throwable =>
        ZIO.succeed(throwable.printStackTrace()).map(_ => ExitCode.failure)
      }
