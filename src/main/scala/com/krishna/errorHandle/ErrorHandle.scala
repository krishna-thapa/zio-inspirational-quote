package com.krishna.errorHandle

import java.io.IOException

import org.postgresql.util.PSQLException
import zio.http.Response
import zio.http.model.Status
import zio.{ UIO, ZIO }

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

  // ================================================================================

  private def logAndHttpResponse(errorMsg: String, exception: Throwable): UIO[Response] =
    ZIO.logError(
      s"$errorMsg, error message: ${exception.getMessage}"
    ) *>
      ZIO.succeed(Response.text(errorMsg).setStatus(Status.InternalServerError))

  val responseAuthError: UIO[Response] =
    ZIO.logError(
      "Failed while authorizing the user, User not allowed!"
    ) *>
      ZIO.succeed(Response.text("User not allowed!").setStatus(Status.Unauthorized))

  def responseError(service: String, exception: Throwable): UIO[Response] =
    exception match
      case ex: PSQLException =>
        logAndHttpResponse(s"Failed in database while running $service service.", ex)
      case ex                => logAndHttpResponse(s"Failed while running $service", ex)
