package com.krishna.http.api

import zio.*
import zio.http.*
import zio.http.model.{ HttpError, Method }
import zio.json.*

import com.krishna.auth.{ AuthService, LoginForm }
import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.model.user.RegisterUser

object AuthHttp:

  def apply(): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.POST -> !! / "user" / "login" =>
          for
            loginForm <- req.body.asString.map(_.fromJson[LoginForm])
            response  <- AuthService.loginResponse(loginForm)
          yield response
        case req@Method.POST -> !! / "user" / "register" =>
          for
            userForm <- req.body.asString.map(_.fromJson[RegisterUser])
            response <- UserService.registerUser(userForm)
          yield response
      }
