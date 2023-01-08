package com.krishna.database.user

import zio.{ Task, ZIO }

import com.krishna.errorHandle.ErrorHandle
import com.krishna.model.user.{ LoginForm, RegisterUser, UserInfo }

trait UserRepo:

  def loginUser(user: LoginForm): Task[Option[UserInfo]]

  def registerUser(user: UserInfo): Task[Int]

  def userInfo(email: String): Task[UserInfo]

  def updateUserInfo(user: RegisterUser): Task[Int]

  def listAllUser: Task[List[UserInfo]]

  def toggleAdminRole(email: String): Task[Int]

  def deleteUser(email: String): Task[Int]

object UserRepo:

  def loginUser(user: LoginForm): ZIO[UserRepo, Throwable, Option[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.loginUser(user))

  def registerOrUpdate(userForm: RegisterUser, isUpdate: Boolean): ZIO[UserRepo, Throwable, Int] =
    if isUpdate then ZIO.serviceWithZIO[UserRepo](_.updateUserInfo(userForm))
    else ZIO.serviceWithZIO[UserRepo](_.registerUser(UserInfo(userForm)))

  def userInfo(email: String): ZIO[UserRepo, Throwable, UserInfo] =
    ZIO.serviceWithZIO[UserRepo](_.userInfo(email))

  def users: ZIO[UserRepo, Throwable, List[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.listAllUser)

  def toggleAdminRole(email: String): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.toggleAdminRole(email))

  def deleteUser(email: String): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.deleteUser(email))
