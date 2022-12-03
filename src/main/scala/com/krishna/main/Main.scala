package com.krishna.main

import zio.*
import zio.logging.{ LogFilter, LogFormat, console }

object Main extends ZIOAppDefault:

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

  override val run: ZIO[Any, Nothing, ExitCode] =
    for {
      _ <- ZIO.logInfo("Running ZIO inspirational quote project!!")
    } yield ExitCode.success
