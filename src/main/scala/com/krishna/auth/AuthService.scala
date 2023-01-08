package com.krishna.auth

import zio.http.Response
import zio.http.model.{ HttpError, Status }
import zio.{ Task, ZIO }

object AuthService:

  def loginResponse(loginForm: Either[String, LoginForm]): Task[Response] =
    loginForm match
      case Right(login) =>
        if login.password == "123" then
          ZIO.succeed(
            Response
              .text("Login success!!")
              .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(login.userName))
          )
        else
          val errorMsg: String = s"Invalid login user form for username ${login.userName}"
          ZIO
            .logError(errorMsg)
            .as(Response.text(errorMsg).setStatus(Status.Unauthorized))
      case Left(error)  =>
        val errorMsg: String = s"Failed to parse the user login form input, error: $error"
        ZIO
          .logError(errorMsg)
          .as(Response.text(errorMsg).setStatus(Status.BadRequest))
