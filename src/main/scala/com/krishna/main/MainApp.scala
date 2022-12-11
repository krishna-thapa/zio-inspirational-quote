package com.krishna.main

import java.io.IOException
import java.util.UUID

import zhttp.http._
import zhttp.service.Server
import zio.logging.{ LogFilter, LogFormat, console }
import zio.{ ExitCode, ZIO, ZIOAppDefault, * }

import com.krishna.config.EnvironmentConfig
import com.krishna.http.{ AdminHttp, HomePage }
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

  val port: Int = 9000

  val combinedHttp: Http[WebClient & EnvironmentConfig, Throwable, Request, Response] =
    HomePage() ++ AdminHttp()

  val program: ZIO[WebClient & EnvironmentConfig, Throwable, Unit] =
    for
      _ <- ZIO.logInfo("Running ZIO inspirational quote API project!!")
      _ <- ZIO.logInfo(s"Starting server on http://localhost:$port")
      _ <- Server.start(port, combinedHttp)
    yield ()

  // =========================================
  def logAndFail(errorMsg: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    ZIO.logError(errorMsg) *> ZIO.fail(exception)

  // Catch IOException && CharacterCodingException && Throwable
  def errorHandler(exception: Throwable): ZIO[Any, Throwable, Unit] =
    exception match
      case ex: IOException => logAndFail("Failed while reading the CSV file!", ex)
      case ex              => logAndFail("Generic fail", ex)

  override val run: ZIO[Environment & (ZIOAppArgs & Scope), Any, Any] =
    program
      .provide(WikiHttpService.layer, EnvironmentConfig.layer)
      .catchAll(errorHandler)
      .map(_ => ExitCode.success)
