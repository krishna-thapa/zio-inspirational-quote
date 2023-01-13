package com.krishna.http.api.user

import zio.ZIO
import zio.http.*
import zio.http.model.Method
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ JwtUser, RegisterUser }

object UserAuthHttp:

  def apply(claim: JwtUser): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.PUT -> !! / "user" / "update" =>
          for
            userForm <- req.body.asString.map(_.fromJson[RegisterUser])
            response <- UserService
              .updateUser(userForm, claim.email)
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        case Method.GET -> !! / "user" / "info" =>
          for response <- UserService
              .getUserInfo(claim.email)
              .catchAll(ErrorHandle.responseError("updateUser", _))
          yield response

        // TODO https://github.com/zio/zio-http/pull/1617
        case req @ Method.POST -> !! / "user" / "picture" =>
          for
            userPicture <- req.body.asArray
            response    <- UserService
              .uploadPicture(claim.email, userPicture)
              .catchAll(ErrorHandle.responseError("uploadPicture", _))
          yield response

        case Method.GET -> !! / "user" / "picture" =>
          for response <- UserService
              .getUserPicture(claim.email)
              .catchAll(ErrorHandle.responseError("downloadPicture", _))
          yield response

        case Method.DELETE -> !! / "user" / "picture" =>
          for response <- UserService
              .deleteUserPicture(claim.email)
              .catchAll(ErrorHandle.responseError("deletePicture", _))
          yield response
      }
