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

  // Need to check if the user has enter wrong password but has an account already
  private def validateUser(
    email: String,
    onSuccessResponse: ZIO[UserRepo, Throwable, Response],
    isRegister: Boolean = false
  ): ZIO[UserRepo, Throwable, Response] =
    UserRepo.isAccountExist(email).flatMap {
      case Some(_) =>
        if !isRegister then onSuccessResponse
        else responseWithLog(s"Account already present for the email: $email", Status.Conflict)
      case None    =>
        if !isRegister then
          responseWithLog(s"Account not found for the email: $email", Status.Unauthorized)
        else onSuccessResponse
    }

  /**
   * User login with the password and email as username
   * @param loginForm
   *   With password and email address
   * @return
   *   Success or failure while login
   */
  def loginResponse(loginForm: Either[String, LoginForm]): ZIO[UserRepo, Throwable, Response] =
    loginForm match
      case Right(login) if LoginForm.validateEmail(login.email) =>
        lazy val loginUser: ZIO[UserRepo, Throwable, Response] =
          for res <- UserRepo.loginUser(login)
          yield
            if res.isDefined then
              Response
                .text("Login success!!")
                .addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(JwtUser(res.get)))
            else
              Response
                .text(s"Invalid login password for user with email: ${login.email}")
                .setStatus(Status.Unauthorized)

        validateUser(login.email, loginUser)
      case Right(login)                                         =>
        val errorMsg: String = s"Invalid email pattern for email: ${login.email}"
        responseWithLog(errorMsg, Status.BadRequest)
      case Left(error)                                          =>
        val errorMsg: String = s"Failed to parse the user login form input, error: $error"
        responseWithLog(errorMsg, Status.BadRequest)

  /**
   * Register the new user in the database
   * @param userForm
   *   User registration form details
   * @return
   *   Success or failure while adding new user
   */
  def registerUser(
    userForm: Either[String, RegisterUser]
  ): ZIO[UserRepo, Throwable, Response] =
    userForm match
      case Right(form) =>
        if validateForm(form) then
          lazy val registerUserResponse: ZIO[UserRepo, Throwable, Response] = for
            res      <- UserRepo.registerUser(form)
            validRes <- validateDatabaseResponse(res, "inserting a user record")
          yield
            if validRes.status == Status.Ok
            then validRes.addHeader("X-ACCESS-TOKEN", JwtService.jwtEncode(JwtUser(form)))
            else validRes

          validateUser(form.email, registerUserResponse, isRegister = true)
        else
          val errorMsg: String =
            s"Invalid register user form. Fields should be non-empty with valid email and password with minimum length of 3."
          responseWithLog(errorMsg, Status.BadRequest)
      case Left(error) =>
        val errorMsg: String = s"Failed to parse the user register form input, error: $error"
        responseWithLog(errorMsg, Status.BadRequest)

  /**
   * Update the user information in the database
   * @param userForm
   *   Updated form that contains updated information
   * @param email
   *   Email of the user
   * @return
   *   Success or failure while updating the user info
   */
  // TODO: Maybe the user is allowed to upload the email address also??
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

  /**
   * Get all the information related to the user
   * @param email
   *   Email of the user
   * @return
   *   Success of failure of the user info
   */
  // TODO: Only retrieve the relevant info, don't need password, UUID and isadmin field
  def getUserInfo(email: String): ZIO[UserRepo, Throwable, Response] =
    for res <- UserRepo.userInfo(email)
    yield ConfigHttp.convertToJson(res)

  /**
   * Get all the users and are sort them by the first names
   * @return
   *   List of the users
   */
  def getAllUserInfo: ZIO[UserRepo, Throwable, Response] =
    for
      res <- UserRepo.users
      _   <-
        if res.isEmpty then ZIO.logWarning("Database is empty!")
        else ZIO.logInfo(s"Success on returning total ${res.length} users from database!")
    yield ConfigHttp.convertToJson(res)

  /**
   * Toggle the user to the admin role from the user role and vice-versa
   * @param email
   *   email of the user
   * @return
   *   Success or failure while toggling the user role
   */
  // TODO: Does this need to return a new JWT token that has field of admin as true or false
  def toggleAdminRole(email: String): ZIO[UserRepo, Throwable, Response] =
    val response: String => ZIO[UserRepo, Throwable, Response] = email =>
      for
        res      <- UserRepo.toggleAdminRole(email)
        validRes <- validateDatabaseResponse(res, "toggle to/from Admin role")
      yield validRes

    validateEmailAndResponse(email, response)

  /**
   * Delete the user from the database, only the user that have admin access can perform this
   * action
   * @param email
   *   email of the user
   * @return
   *   Success or failure while deleting the user
   */
  def deleteUser(email: String): ZIO[UserRepo, Throwable, Response] =
    val response: String => ZIO[UserRepo, Throwable, Response] = email =>
      for
        res      <- UserRepo.deleteUser(email)
        validRes <- validateDatabaseResponse(res, "delete a user record")
      yield validRes

    validateEmailAndResponse(email, response)

  /**
   * Upload a picture for the profile photo of the user account
   * @param email
   *   Email of the user
   * @param picture
   *   Picture to be uploaded
   * @return
   *   Success or failure while uploading the picture
   */
  def uploadPicture(email: String, picture: Array[Byte]): ZIO[UserRepo, Throwable, Response] =
    for
      res      <- UserRepo.uploadPicture(email, picture)
      validRes <- validateDatabaseResponse(res, "upload a user's profile picture")
    yield validRes

  /**
   * Get the uploaded picture from the database
   * @param email
   *   Email of the user
   * @return
   *   Success or failure while retrieving the picture from the database
   */
  def getUserPicture(email: String): ZIO[UserRepo, Throwable, Response] =
    for res <- UserRepo.getPicture(email)
    yield
      if res.isEmpty then
        Response
          .text("No profile picture found!")
          .setStatus(Status.NotFound)
      else
        val result: Array[Byte] = res.fold(Array.empty[Byte])(identity)
        Response(
          status = Status.Ok,
          headers = Headers.contentLength(result.length.toLong),
          body = Body.fromStream(ZStream.fromIterable(result))
        ).setHeaders(Headers.contentType("image/jpeg"))
          .setHeaders(Headers.contentDisposition(s"attachment; filename=$email.jpg"))

  /**
   * Delete the user picture from the database
   * @param email
   *   Email of the user
   * @return
   *   Success or failure while deleting the user picture
   */
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
