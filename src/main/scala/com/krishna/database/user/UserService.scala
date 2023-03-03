package com.krishna.database.user

import zio.http.*
import zio.http.model.{ Headers, Status }
import zio.stream.ZStream
import zio.{ Task, UIO, ZIO }

import com.krishna.auth.JwtService
import com.krishna.http.ConfigHttp
import com.krishna.model.user.RegisterUser.validateForm
import com.krishna.model.user.traits.Email
import com.krishna.model.user.{ JwtUser, LoginForm, RegisterUser }

object UserService:

  private val responseWithLog: (String, Status) => UIO[Response] = (message, status) =>
    ZIO
      .logError(message)
      .as(Response.text(message).setStatus(status))

  def loginResponse(loginForm: Either[String, LoginForm]): ZIO[UserRepo, Throwable, Response] =
    loginForm match
      case Right(login) if LoginForm.validateEmail(login.email) =>
        for res <- UserRepo.loginUser(login)
        yield
          if res.isDefined then
            Response
              .text("Login success!!")
              .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(JwtUser(res.get)))
          else
            Response
              .text(s"Invalid login user form for username ${login.email}")
              .setStatus(Status.Unauthorized)
      case Right(login)                                         =>
        val errorMsg: String = s"Invalid email pattern for email: ${login.email}"
        responseWithLog(errorMsg, Status.BadRequest)
      case Left(error)                                          =>
        val errorMsg: String = s"Failed to parse the user login form input, error: $error"
        responseWithLog(errorMsg, Status.BadRequest)

  def registerUser(
    userForm: Either[String, RegisterUser]
  ): ZIO[UserRepo, Throwable, Response] =
    userForm match
      case Right(form) =>
        if validateForm(form) then
          for
            res      <- UserRepo.registerUser(form)
            validRes <- validateDatabaseResponse(res, "inserting a user record")
          yield
            if validRes.status == Status.Ok
            then validRes.addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(JwtUser(form)))
            else validRes
        else
          val errorMsg: String =
            s"Invalid register user form. Fields should be non-empty with valid email and password with minimum length of 3."
          responseWithLog(errorMsg, Status.BadRequest)
      case Left(error) =>
        val errorMsg: String = s"Failed to parse the user register form input, error: $error"
        responseWithLog(errorMsg, Status.BadRequest)

  def updateUser(
    userForm: Either[String, RegisterUser],
    email: String
  ): ZIO[UserRepo, Throwable, Response] = userForm match
    case Right(form) if form.email == email && validateForm(form) =>
      for
        res      <- UserRepo.updateUserInfo(form)
        validRes <- validateDatabaseResponse(res, "updating a user record")
      yield
        if validRes.status == Status.Ok
        then validRes.addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(JwtUser(form)))
        else validRes
    case Right(_)                                                 =>
      val errorMsg: String =
        s"Invalid user form. Fields should be non-empty with valid email and password with minimum length of 3."
      responseWithLog(errorMsg, Status.BadRequest)
    case Left(error)                                              =>
      val errorMsg: String = s"Failed to parse the user register form input, error: $error"
      responseWithLog(errorMsg, Status.BadRequest)

  def getUserInfo(email: String): ZIO[UserRepo, Throwable, Response] =
    for res <- UserRepo.userInfo(email)
    yield ConfigHttp.convertToJson(res)

  def getAllUserInfo(): ZIO[UserRepo, Throwable, Response] =
    for
      res <- UserRepo.users
      _   <-
        if res.isEmpty then ZIO.logWarning("Database is empty!")
        else ZIO.logInfo(s"Success on returning total ${res.length} users from database!")
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

  def uploadPicture(email: String, picture: Array[Byte]): ZIO[UserRepo, Throwable, Response] =
    for
      res      <- UserRepo.uploadPicture(email, picture)
      validRes <- validateDatabaseResponse(res, "upload a user's profile picture")
    yield validRes

  def getUserPicture(email: String): ZIO[UserRepo, Throwable, Response] =
    for res <- UserRepo.getPicture(email)
    yield
      if res.isEmpty then
        Response
          .text("No profile picture found!")
          .setStatus(Status.NotFound)
      else
        Response(
          status = Status.Ok,
          headers = Headers.contentLength(res.get.length.toLong),
          body = Body.fromStream(ZStream.fromIterable(res.get))
        ).setHeaders(Headers.contentType("image/jpeg"))
          .setHeaders(Headers.contentDisposition(s"attachment; filename=$email.jpg"))

  def deleteUserPicture(email: String): ZIO[UserRepo, Throwable, Response] =
    for
      res      <- UserRepo.deletePicture(email)
      validRes <- validateDatabaseResponse(res, "delete a user picture")
    yield validRes

  private def validateEmailAndResponse(
    email: String,
    responseFun: String => ZIO[UserRepo, Throwable, Response]
  ): ZIO[UserRepo, Throwable, Response] =
    if email.nonEmpty && Email.validEmail(email) then responseFun(email)
    else
      val errorMsg: String =
        s"Invalid pattern of the email address: $email."
      responseWithLog(errorMsg, Status.BadRequest)

  private def validateDatabaseResponse(response: Int, service: String): UIO[Response] =
    if response != 1 then
      val errorMsg: String = s"Invalid response from the Postgres service while $service"
      responseWithLog(errorMsg, Status.InternalServerError)
    else
      val successMsg: String = s"$service success!!"
      ZIO
        .logInfo(successMsg)
        .as(Response.text(successMsg))
