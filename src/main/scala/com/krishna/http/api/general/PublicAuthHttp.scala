package com.krishna.http.api.general

import zio.*
import zio.http.*
import zio.http.model.{ Method, Status }
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ LoginForm, RegisterUser }

object PublicAuthHttp:

  private val logResponse: (Status, String) => UIO[Unit] = (status, httpService) =>
    if status != Status.Ok then ZIO.logError(s"Http response error on service called: $httpService")
    else ZIO.logInfo(s"Success on the Http service call: $httpService")

  def apply(): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.POST -> !! / "user" / "login" =>
          for
            loginForm <- req.body.asString.map(_.fromJson[LoginForm])
            response  <- UserService
              .loginResponse(loginForm)
              .catchAll(ErrorHandle.responseError("loginUser", _))
            _         <- logResponse(response.status, "user login")
          yield response

        case req @ Method.POST -> !! / "user" / "register" =>
          for
            userForm <- req.body.asString.map(_.fromJson[RegisterUser])
            response <- UserService
              .registerUser(userForm)
              .catchAll(ErrorHandle.responseError("registerUser", _))
            _        <- logResponse(response.status, "user register")
          yield response
      }
