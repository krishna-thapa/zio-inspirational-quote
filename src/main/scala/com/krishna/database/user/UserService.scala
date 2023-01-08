package com.krishna.database.user

import zio.http.Response
import zio.http.model.Status
import zio.{ Task, UIO, ZIO }
import com.krishna.auth.JwtService
import com.krishna.http.ConfigHttp
import com.krishna.model.user.RegisterUser.validateForm
import com.krishna.model.user.{ Email, LoginForm, RegisterUser }

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
          for
            res      <- UserRepo.registerOrUpdate(form, isUpdate)
            validRes <- validateDatabaseResponse(res, "inserting or updating a user record")
          yield
            if validRes.status == Status.Ok
            then validRes.addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(form.email))
            else validRes
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

  def getUserInfo(email: String): ZIO[UserRepo, Throwable, Response] =
    val response: String => ZIO[UserRepo, Throwable, Response] = email =>
      for res <- UserRepo.userInfo(email)
      yield ConfigHttp.convertToJson(res)

    validateEmailAndResponse(email, response)

  def getAllUserInfo(): ZIO[UserRepo, Throwable, Response] =
    for res <- UserRepo.users
    yield ConfigHttp.convertToJson(res)

  // TODO: Does this need to return a new JWT token that has field of admin as true or false
  def toggleAdminRole(email: String): ZIO[UserRepo, Throwable, Response] =
    val response: String => ZIO[UserRepo, Throwable, Response] = email =>
      for
        res      <- UserRepo.toggleAdminRole(email)
        validRes <- validateDatabaseResponse(res, "toggle to Admin role")
      yield validRes

    validateEmailAndResponse(email, response)

  def deleteUser(email: String): ZIO[UserRepo, Throwable, Response] =
    val response: String => ZIO[UserRepo, Throwable, Response] = email =>
      for
        res      <- UserRepo.deleteUser(email)
        validRes <- validateDatabaseResponse(res, "delete a user record")
      yield validRes

    validateEmailAndResponse(email, response)

  private def validateEmailAndResponse(
    email: String,
    responseFun: String => ZIO[UserRepo, Throwable, Response]
  ): ZIO[UserRepo, Throwable, Response] =
    if email.nonEmpty && Email.validEmail(email) then responseFun(email)
    else
      val errorMsg: String =
        s"Invalid pattern of the email address: $email."
      ZIO
        .logError(errorMsg)
        .as(Response.text(errorMsg).setStatus(Status.BadRequest))

  private def validateDatabaseResponse(response: Int, service: String): UIO[Response] =
    if response != 1 then
      val errorMsg: String = s"Invalid response from the Postgres service while $service"
      ZIO
        .logError(errorMsg)
        .as(
          Response
            .text(errorMsg)
            .setStatus(Status.InternalServerError)
        )
    else
      val successMsg: String = s"$service success!!"
      ZIO
        .logError(successMsg)
        .as(Response.text(successMsg))
