package com.krishna.http.api.admin

import zio.*
import zio.http.Middleware.cors
import zio.http.*
import zio.http.model.Method
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.http.api.CorsConfigQuote
import com.krishna.model.user.JwtUser

object AdminAuthHttp extends CorsConfigQuote:

  def apply(claim: JwtUser): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case Method.GET -> !! / "admin" / "users" =>
          for response <- UserService
              .getAllUserInfo
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        case Method.POST -> !! / "admin" / "user" / "toggle-to-admin" / email =>
          for response <- UserService
              .toggleAdminRole(email)
              .catchAll(ErrorHandle.responseError("ToggleAdminRole", _))
          yield response

        case Method.DELETE -> !! / "admin" / "user" / email =>
          for response <- UserService
              .deleteUser(email)
              .catchAll(ErrorHandle.responseError("DeleteUser", _))
          yield response
      } @@ cors(config)
