package com.krishna.http.api

import zio.*
import zio.http.*
import zio.http.model.{ HttpError, Method }
import zio.json.*

import com.krishna.auth.AuthService
import com.krishna.auth.model.LoginForm

object AuthHttp:

  def apply(): Http[Any, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.POST -> !! / "login" =>
          for
            loginForm <- req.body.asString.map(_.fromJson[LoginForm])
            response  <- AuthService.loginResponse(loginForm)
          yield response
      }
