package com.krishna.http.api

import zio.*
import zio.http.*
import zio.http.model.{ Headers, HttpError, Method, Status }
import zio.json.*
import zio.stream.ZStream

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ LoginForm, RegisterUser }

object AuthHttp:

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

        case req @ Method.PUT -> !! / "user" / "update" =>
          for
            userForm <- req.body.asString.map(_.fromJson[RegisterUser])
            response <- UserService
              .registerOrUpdateUser(userForm, isUpdate = true)
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        case Method.GET -> !! / "user" / email =>
          for response <- UserService
              .getUserInfo(email)
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        case Method.GET -> !! / "users" =>
          for response <- UserService
              .getAllUserInfo()
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

        // TODO https://github.com/zio/zio-http/pull/1617
        case req @ Method.POST -> !! / "user" / "picture" / email =>
          for
            userPicture <- req.body.asArray
            response    <- UserService
              .uploadPicture(email, userPicture)
              .catchAll(ErrorHandle.responseError("uploadPicture", _))
          yield response

        case Method.GET -> !! / "user" / "picture" / email =>
          for response <- UserService
              .getUserPicture(email)
              .catchAll(ErrorHandle.responseError("downloadPicture", _))
          yield response

        case Method.DELETE -> !! / "user" / "picture" / email =>
          for response <- UserService
              .deleteUserPicture(email)
              .catchAll(ErrorHandle.responseError("deletePicture", _))
          yield response
      }
