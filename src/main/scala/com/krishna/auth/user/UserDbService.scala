package com.krishna.auth.user

import zio.Task

import com.krishna.auth.model.UserInfo
import com.krishna.auth.user.SqlUser.*
import com.krishna.config.Configuration
import com.krishna.util.DbUtils
import com.krishna.util.sqlCommon.*

case class UserDbService() extends UserRepo:

  override def registerUser(user: UserInfo): Task[Int] = ???

  override def lookupUser(id: String): Task[Option[UserInfo]] = ???

  override def listAllUser: Task[List[UserInfo]] = ???
