package com.krishna.database.user

import zio.{ Task, ULayer, ZIO, ZLayer }

import com.krishna.auth.BcryptObject
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
      validatePassword = response.exists(userInfo =>
        BcryptObject.validatePassword(user.password, userInfo.password).getOrElse(false)
      )
    yield if validatePassword then response else None

  override def registerUser(user: UserInfo): Task[Int] =
    for
      tableName      <- getUserTable
      hashedPassword <- ZIO.fromTry(BcryptObject.encryptPassword(user.password))
      response       <- runUpdateTxa(insertUser(tableName, user.copy(password = hashedPassword)))
    yield response

  override def userInfo(email: String): Task[UserInfo] =
    for
      tableName <- getUserTable
      response  <- runQueryTxa(getUser(tableName, email))
    yield response

  override def updateUserInfo(user: RegisterUser): Task[Int] =
    for
      tableName      <- getUserTable
      hashedPassword <- ZIO.fromTry(BcryptObject.encryptPassword(user.password))
      response       <- runUpdateTxa(updateUser(tableName, user.copy(password = hashedPassword)))
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

  override def uploadPicture(email: String, picture: Array[Byte]): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(addPicture(tableName, email, picture))
    yield response

  override def getPicture(email: String): Task[Option[Array[Byte]]] =
    for
      tableName <- getUserTable
      response  <- runQueryTxa(downloadPicture(tableName, email))
    yield response

  override def deletePicture(email: String): Task[Int] =
    for
      tableName <- getUserTable
      response  <- runUpdateTxa(removePicture(tableName, email))
    yield response

object UserDbService:
  val layer: ULayer[UserDbService] = ZLayer.succeed(UserDbService())
