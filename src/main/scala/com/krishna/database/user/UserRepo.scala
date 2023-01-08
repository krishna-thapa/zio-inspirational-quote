package com.krishna.database.user

import zio.{ Task, ZIO }

import com.krishna.model.user.{ RegisterUser, UserInfo }

trait UserRepo:

  def registerUser(user: UserInfo): Task[Int]

  def lookupUser(id: String): Task[Option[UserInfo]]

  def listAllUser: Task[List[UserInfo]]

  // def toggleAdmin()

object UserRepo:

  def register(userForm: RegisterUser): ZIO[UserRepo, Throwable, Int] =
    ZIO.serviceWithZIO[UserRepo](_.registerUser(UserInfo(userForm)))

  def lookup(id: String): ZIO[UserRepo, Throwable, Option[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.lookupUser(id))

  def users: ZIO[UserRepo, Throwable, List[UserInfo]] =
    ZIO.serviceWithZIO[UserRepo](_.listAllUser)
