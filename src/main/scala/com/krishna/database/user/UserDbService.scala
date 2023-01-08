package com.krishna.database.user

import zio.{ Task, ULayer, ZLayer }

import com.krishna.model.user.{ LoginForm, RegisterUser, UserInfo }
import com.krishna.util.DbUtils
import com.krishna.util.DbUtils.getUserTable
import com.krishna.util.sqlCommon.*

import SqlUser.*

case class UserDbService() extends UserRepo:

  override def loginUser(user: LoginForm): Task[Option[UserInfo]] =
    for
      tableName <- getUserTable
      response  <- runQueryTxa(validateUser(tableName, user))
    yield response

  override def registerUser(user: UserInfo): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(insertUser(tableName, user))
    yield response

  override def userInfo(email: String): Task[UserInfo] =
    for
      tableName <- getUserTable
      response  <- runQueryTxa(getUser(tableName, email))
    yield response

  override def updateUserInfo(user: RegisterUser): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(updateUser(tableName, user))
    yield response

  override def listAllUser: Task[List[UserInfo]] =
    for
      tableName <- getUserTable
      response  <- runQueryTxa(getAllUsers(tableName))
    yield response

  override def toggleAdminRole(email: String): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(adminRole(tableName, email))
    yield response

  override def deleteUser(email: String): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(delete(tableName, email))
    yield response

object UserDbService:
  val layer: ULayer[UserDbService] = ZLayer.succeed(UserDbService())
