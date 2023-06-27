package com.krishna.http.api.user

import zio.ZIO
import zio.http.*
import zio.http.forms.FormData
import zio.http.model.{ Method, Status }
import zio.json.*

import com.krishna.auth.JwtService
import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ JwtUser, RegisterUser }

object UserAuthHttp:

  def apply(): Http[UserRepo, Throwable, Request, Response] =
    Http
      .collectZIO[Request] {
        case req @ Method.PUT -> !! / "user" / "update" =>
          JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
            for
              userForm <- req.body.asString.map(_.fromJson[RegisterUser])
              response <- UserService
                .updateUser(userForm, claim.email)
                .catchAll(ErrorHandle.responseError("updateUser", _))
            yield response
          }

        case req @ Method.GET -> !! / "user" / "info" =>
          JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
            for response <- UserService
                .getUserInfo(claim.email)
                .catchAll(ErrorHandle.responseError("updateUser", _))
            yield response
          }

        case req @ Method.POST -> !! / "user" / "picture" =>
          JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
            for
              form     <- req.body.asMultipartForm
              response <- form.get("image") match
                case Some(image) =>
                  image match
                    case FormData.Binary(_, data, contentType, transferEncoding, filename)
                         if contentType.binary && contentType.mainType.contains("image") =>
                      UserService
                        .uploadPicture(claim.email, data.toArray)
                        .catchAll(ErrorHandle.responseError("uploadPicture", _))
                    case _ =>
                      ZIO.succeed(
                        Response(
                          Status.BadRequest,
                          body = Body.fromString("Parameter 'image' must be a binary image file")
                        )
                      )
                case None        =>
                  ZIO.succeed(
                    Response(Status.BadRequest, body = Body.fromString("Missing 'image' from body"))
                  )
            yield response
          }

        case req @ Method.GET -> !! / "user" / "picture" =>
          JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
            for response <- UserService
                .getUserPicture(claim.email)
                .catchAll(ErrorHandle.responseError("downloadPicture", _))
            yield response
          }

        case req @ Method.DELETE -> !! / "user" / "picture" =>
          JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
            for response <- UserService
                .deleteUserPicture(claim.email)
                .catchAll(ErrorHandle.responseError("deletePicture", _))
            yield response
          }
      }
