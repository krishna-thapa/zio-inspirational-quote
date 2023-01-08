package com.krishna.database.user

import zio.http.Response
import zio.http.model.Status
import zio.{ Task, ZIO }

import com.krishna.auth.JwtService
import com.krishna.model.user.RegisterUser
import com.krishna.model.user.RegisterUser.validateForm

object UserService:

  def registerUser(userForm: Either[String, RegisterUser]): ZIO[UserRepo, Throwable, Response] =
    userForm match
      case Right(form) =>
        if validateForm(form) then
          for _ <- UserRepo.register(form)
          yield Response
            .text("Register success!!")
            .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(form.email))
        else
          val errorMsg: String =
            s"Invalid register user form. Fields should be non-empty with valid email and password."
          ZIO
            .logError(errorMsg)
            .as(Response.text(errorMsg).setStatus(Status.BadRequest))
      case Left(error) =>
        val errorMsg: String = s"Failed to parse the user register form input, error: $error"
        ZIO
          .logError(errorMsg)
          .as(Response.text(errorMsg).setStatus(Status.BadRequest))
