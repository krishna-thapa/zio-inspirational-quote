package com.krishna.database.user

import zio.http.Response
import zio.http.model.Status
import zio.{ Task, ZIO }

import com.krishna.auth.JwtService
import com.krishna.model.user.RegisterUser.validateForm
import com.krishna.model.user.{ LoginForm, RegisterUser }

object UserService:

  def loginResponse(loginForm: Either[String, LoginForm]): ZIO[UserRepo, Throwable, Response] =
    loginForm match
      case Right(login) if LoginForm.validateEmail(login.email) =>
        for res <- UserRepo.loginUser(login)
        yield
          if res.isDefined then
            Response
              .text("Login success!!")
              .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(login.email))
          else
            Response
              .text(s"Invalid login user form for username ${login.email}")
              .setStatus(Status.Unauthorized)
      case Right(login)                                         =>
        val errorMsg: String = s"Invalid email pattern for email: ${login.email}"
        ZIO
          .logError(errorMsg)
          .as(Response.text(errorMsg).setStatus(Status.BadRequest))
      case Left(error)                                          =>
        val errorMsg: String = s"Failed to parse the user login form input, error: $error"
        ZIO
          .logError(errorMsg)
          .as(Response.text(errorMsg).setStatus(Status.BadRequest))

  def registerOrUpdateUser(
    userForm: Either[String, RegisterUser],
    isUpdate: Boolean = false
  ): ZIO[UserRepo, Throwable, Response] =
    userForm match
      case Right(form) =>
        if validateForm(form) then
          for res <- UserRepo.registerOrUpdate(form, isUpdate)
          yield
            if res != 1 then
              Response
                .text(
                  s"Invalid response from the Postgres service while inserting or updating user: ${form.email}"
                )
                .setStatus(Status.InternalServerError)
            else
              Response
                .text("Register/Update success!!")
                .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(form.email))
        else
          val errorMsg: String =
            s"Invalid register user form. Fields should be non-empty with valid email and password with minimum length of 3."
          ZIO
            .logError(errorMsg)
            .as(Response.text(errorMsg).setStatus(Status.BadRequest))
      case Left(error) =>
        val errorMsg: String = s"Failed to parse the user register form input, error: $error"
        ZIO
          .logError(errorMsg)
          .as(Response.text(errorMsg).setStatus(Status.BadRequest))
