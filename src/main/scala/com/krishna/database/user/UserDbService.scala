package com.krishna.database.user

import zio.{ Task, ULayer, ZLayer }

import com.krishna.model.user.UserInfo
import com.krishna.util.DbUtils
import com.krishna.util.DbUtils.getUserTable
import com.krishna.util.sqlCommon.*

import SqlUser.*

case class UserDbService() extends UserRepo:

  override def registerUser(user: UserInfo): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(insertUser(tableName, user))
    yield response

  override def lookupUser(id: String): Task[Option[UserInfo]] = ???

  override def listAllUser: Task[List[UserInfo]] = ???

object UserDbService:
  val layer: ULayer[UserDbService] = ZLayer.succeed(UserDbService())
