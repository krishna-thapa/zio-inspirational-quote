package com.krishna.errorHandle

import java.io.IOException

import zio.ZIO

object ErrorHandle:

  def logAndFail(errorMsg: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    ZIO.logError(
      s"$errorMsg, error message: ${exception.getMessage}"
    ) *>
      ZIO.fail(exception)

  // Catch IOException && CharacterCodingException && Throwable
  def handelError(service: String, exception: Throwable): ZIO[Any, Throwable, Unit] =
    exception match
      case ex: IOException => logAndFail("Failed while reading the CSV file!", ex)
      case ex              => logAndFail(s"Fail on $service", ex)
