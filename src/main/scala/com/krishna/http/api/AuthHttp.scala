package com.krishna.http.api

import com.krishna.model.auth.Login
import com.krishna.auth.AuthService
import zio.*
import zio.http.*
import zio.http.model.{ HttpError, Method }
import zio.json.*

object AuthHttp:

  def apply(): Http[Any, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "login" =>
        for
          login    <- req.body.asString.map(_.fromJson[Login])
          response <-
            login match
              case Right(login)   =>
                if login.password == "123"
                then ZIO.succeed(Response.text(AuthService.jwtEncode(login.userName)))
                else
                  ZIO.succeed(
                    Response.fromHttpError(HttpError.Unauthorized("Invalid username of password\n"))
                  )
              case Left(errorMsg) =>
                ZIO.succeed(
                  Response.fromHttpError(
                    HttpError.BadRequest(s"Invalid input parameters!, with error: $errorMsg")
                  )
                )
        yield response
    }
