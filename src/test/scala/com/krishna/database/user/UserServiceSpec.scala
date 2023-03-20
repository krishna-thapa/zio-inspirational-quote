package com.krishna.database.user

import zio.http.Response
import zio.test.*
import zio.{ Task, ULayer, ZIO, ZLayer }

import com.krishna.model.user.{ LoginForm, RegisterUser, UserInfo }

object UserServiceSpec extends ZIOSpecDefault:

  val mockedUser: UserInfo = UserInfo.apply(RegisterUser("first", "last", "abc@com", "password"))

  case class MockedUserRepo() extends UserRepo:
    override def loginUser(user: LoginForm): Task[Option[UserInfo]] = ZIO.attempt(Some(mockedUser))

    override def isAccountExist(email: String): Task[Option[UserInfo]] =
      ZIO.attempt(Some(mockedUser))

    override def registerUser(user: UserInfo): Task[Int] = ZIO.attempt(1)

    override def userInfo(email: String): Task[UserInfo] = ???

    override def updateUserInfo(user: RegisterUser): Task[Int] = ???

    override def listAllUser: Task[List[UserInfo]] = ???

    override def toggleAdminRole(email: String): Task[Int] = ???

    override def deleteUser(email: String): Task[Int] = ???

    override def uploadPicture(email: String, picture: Array[Byte]): Task[Int] = ???

    override def getPicture(email: String): Task[Option[Array[Byte]]] = ???

    override def deletePicture(email: String): Task[Int] = ???

  object MockedUserRepo:
    val layer: ULayer[MockedUserRepo] = ZLayer.succeed(MockedUserRepo())

    val layer1: ULayer[MockedUserRepo] = ZLayer.succeed(
      new MockedUserRepo():

        override def loginUser(user: LoginForm): Task[Option[UserInfo]] = ZIO.succeed(None)
    )

  val loginForm: Right[Nothing, LoginForm]        = Right(LoginForm("abc@com", "password"))
  val invalidLoginForm: Right[Nothing, LoginForm] = Right(LoginForm("@com", "password"))

  val userForm: Either[String, RegisterUser] = Right(
    RegisterUser("first", "last", "def@com", "password")
  )

  def spec = suite("Spec for the UserService")(
    suite("Provided with first mock layer")(
      test("Should response bad request if login form can't be validate") {
        val loginForm: Left[String, LoginForm]                = Left("Bad login form request")
        val loginResponse: ZIO[UserRepo, Throwable, Response] = UserService.loginResponse(loginForm)
        assertZIO(loginResponse.map(_.status.code))(Assertion.equalTo(400))
      },
      test("Should response bad request if login form has invalid email pattern") {
        val loginResponse: ZIO[UserRepo, Throwable, Response] =
          UserService.loginResponse(invalidLoginForm)
        assertZIO(loginResponse.map(_.status.code))(Assertion.equalTo(400))
      },
      test("Should login the authorised user") {
        val loginResponse: ZIO[UserRepo, Throwable, Response] = UserService.loginResponse(loginForm)
        assertZIO(loginResponse.map(_.status.code))(Assertion.equalTo(200))
        assertZIO(loginResponse.map(_.headers.get("X-ACCESS-TOKEN")))(Assertion.isSome)
      },
      test("Should return conflict response if the new user has existing account") {
        val registerResponse: ZIO[UserRepo, Throwable, Response] =
          UserService.registerUser(userForm.map(_.copy(email = "abc@com")))
        assertZIO(registerResponse.map(_.status.code))(Assertion.equalTo(409))
      },
      test("Should register the new user") {
        val registerResponse: ZIO[UserRepo, Throwable, Response] =
          UserService.registerUser(userForm)
        assertZIO(registerResponse.map(_.status.code))(Assertion.equalTo(200))
        // assertZIO(registerResponse.map(_.headers.get("X-ACCESS-TOKEN")))(Assertion.isSome)
      } @@ TestAspect.ignore
    ).provide(MockedUserRepo.layer),
    suite("Provided with second mock layer")(
      test("Should handle if the login user response is None") {
        val loginResponse: ZIO[UserRepo, Throwable, Response] = UserService.loginResponse(loginForm)
        assertZIO(loginResponse.map(_.status.code))(Assertion.equalTo(401))
      }
    ).provide(MockedUserRepo.layer1)
  )
