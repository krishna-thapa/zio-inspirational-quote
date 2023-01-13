package com.krishna.http.api.general

import zio.*
import zio.http.*
import zio.http.model.{ Headers, HttpError, Method, Status }
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ LoginForm, RegisterUser }

object PublicAuthHttp:

  def apply(): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.POST -> !! / "user" / "login" =>
          for
            loginForm <- req.body.asString.map(_.fromJson[LoginForm])
            response  <- UserService
              .loginResponse(loginForm)
              .catchAll(ErrorHandle.responseError("loginUser", _))
          yield response

        case req @ Method.POST -> !! / "user" / "register" =>
          for
            userForm <- req.body.asString.map(_.fromJson[RegisterUser])
            response <- UserService
              .registerOrUpdateUser(userForm)
              .catchAll(ErrorHandle.responseError("registerUser", _))
          yield response
      }
