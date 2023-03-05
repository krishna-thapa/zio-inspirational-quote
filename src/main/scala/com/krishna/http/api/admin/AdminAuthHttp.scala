package com.krishna.http.api.admin

import zio.*
import zio.http.*
import zio.http.model.Method
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.JwtUser

object AdminAuthHttp:

  def apply(claim: JwtUser): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case Method.GET -> !! / "users" =>
          for response <- UserService
              .getAllUserInfo
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        case Method.POST -> !! / "user" / "toggle-to-admin" / email =>
          for response <- UserService
              .toggleAdminRole(email)
              .catchAll(ErrorHandle.responseError("ToggleAdminRole", _))
          yield response

        case Method.DELETE -> !! / "user" / email =>
          for response <- UserService
              .deleteUser(email)
              .catchAll(ErrorHandle.responseError("DeleteUser", _))
          yield response
      }
