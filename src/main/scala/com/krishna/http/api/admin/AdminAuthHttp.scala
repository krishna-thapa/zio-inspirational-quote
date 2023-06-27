package com.krishna.http.api.admin

import zio.*
import zio.http.*
import zio.http.model.Method
import zio.json.*

import com.krishna.auth.JwtService
import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle

object AdminAuthHttp:

  def apply(): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.GET -> !! / "admin" / "users" =>
          JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
            for response <- UserService
                .getAllUserInfo
                .catchAll(ErrorHandle.responseError("updateUser", _))
            yield response
          }

        case req @ Method.POST -> !! / "admin" / "user" / "toggle-to-admin" / email =>
          JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
            for response <- UserService
                .toggleAdminRole(email)
                .catchAll(ErrorHandle.responseError("ToggleAdminRole", _))
            yield response
          }

        case req @ Method.DELETE -> !! / "admin" / "user" / email =>
          JwtService.authenticate(req, isAdmin = true).fold(ErrorHandle.responseAuthError) { _ =>
            for response <- UserService
                .deleteUser(email)
                .catchAll(ErrorHandle.responseError("DeleteUser", _))
            yield response
          }
      }
