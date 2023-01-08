package com.krishna.auth

import com.github.t3hnar.bcrypt.*
import scala.util.Try

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
  def encryptPassword(password: String): Try[String] =
    password.bcryptSafeBounded(5)

  def validatePassword(password: String, encrypted: String): Try[Boolean] =
    password.isBcryptedSafeBounded(encrypted)
