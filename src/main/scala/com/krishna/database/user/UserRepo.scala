package com.krishna.database.user

import zio.{ Task, ZIO }

import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ LoginForm, RegisterUser, UserInfo }

trait UserRepo:

  def isAccountExist(email: String): Task[Option[UserInfo]]

  def loginUser(user: LoginForm): Task[Option[UserInfo]]

  def registerUser(user: UserInfo): Task[Int]

  def userInfo(email: String): Task[UserInfo]

  def updateUserInfo(user: RegisterUser): Task[Int]

  def listAllUser: Task[List[UserInfo]]

  def toggleAdminRole(email: String): Task[Int]

  def deleteUser(email: String): Task[Int]

  def uploadPicture(email: String, picture: Array[Byte]): Task[Int]

  def getPicture(email: String): Task[Option[Array[Byte]]]

  def deletePicture(email: String): Task[Int]

  def listAllUserEmails: Task[List[String]]

object UserRepo:

  def isAccountExist(email: String): ZIO[UserRepo, Throwable, Option[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.isAccountExist(email))

  def loginUser(user: LoginForm): ZIO[UserRepo, Throwable, Option[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.loginUser(user))

  def registerUser(userForm: RegisterUser): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.registerUser(UserInfo(userForm)))

  def updateUserInfo(userForm: RegisterUser): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.updateUserInfo(userForm))

  def userInfo(email: String): ZIO[UserRepo, Throwable, UserInfo] =
    ZIO.serviceWithZIO[UserRepo](_.userInfo(email))

  def users: ZIO[UserRepo, Throwable, List[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.listAllUser)

  def allUserEmails(): ZIO[UserRepo, Throwable, List[String]] =
    ZIO.serviceWithZIO[UserRepo](_.listAllUserEmails)

  def toggleAdminRole(email: String): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.toggleAdminRole(email))

  def deleteUser(email: String): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.deleteUser(email))

  def uploadPicture(email: String, picture: Array[Byte]): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.uploadPicture(email, picture))

  def getPicture(email: String): ZIO[UserRepo, Throwable, Option[Array[Byte]]] =
    ZIO.serviceWithZIO[UserRepo](_.getPicture(email))

  def deletePicture(email: String): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.deletePicture(email))
