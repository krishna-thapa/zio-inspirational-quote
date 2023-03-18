package com.krishna.auth

import com.github.t3hnar.bcrypt.*
import zio.{ Task, ZIO }

import com.krishna.model.user.UserInfo

object BcryptObject:

  /*
      1. With "salt round" they actually mean the cost factor. The cost factor controls how much
      time is needed to calculate a single BCrypt hash. The higher the cost factor,
      the more hashing rounds are done. Increasing the cost factor by 1 doubles the necessary time.
      The more time is necessary, the more difficult is brute-forcing.
      2. The salt is a random value, and should differ for each calculation, so the result should
      hardly ever be the same, even for equal passwords.
      3. The salt is usually included in the resulting hash-string in readable searchForm.
      So with storing the hash-string you also store the salt.
   */
  def encryptPassword(password: String): Task[String] =
    ZIO.fromTry(password.bcryptSafeBounded(5))

  def validatePassword(password: String, userInfo: Option[UserInfo]): Task[Option[Boolean]] =
    val validatePassword: Option[Task[Boolean]] = userInfo.map { userInfo =>
      ZIO.fromTry(password.isBcryptedSafeBounded(userInfo.password))
    }
    ZIO.foreach(validatePassword)(identity)
