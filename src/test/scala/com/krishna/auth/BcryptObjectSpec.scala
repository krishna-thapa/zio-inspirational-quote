package com.krishna.auth

import java.util.UUID

import scala.annotation.unused

import zio.Task
import zio.test.Assertion.*
import zio.test.*

import com.krishna.model.user.{ RegisterUser, UserInfo }

object BcryptObjectSpec extends ZIOSpecDefault:

  def spec = suite("Suite for BcryptObject")(
    test("should encrypt Password") {
      val toTest: Task[String] = BcryptObject.encryptPassword("abcdef")
      assertZIO(toTest)(Assertion.isNonEmptyString)
    },
    test("should fail if the password length is greater than 71 character") {
      val password             = (1 to 72).map(_ => "a").mkString("")
      val toTest: Task[String] = BcryptObject.encryptPassword(password)
      assertZIO(toTest.exit)(Assertion.failsWithA[IllegalArgumentException])
    },
    test("should validate the correct password") {
      for
        encryptPassword <- BcryptObject.encryptPassword("abc")
        userInfo = Some(UserInfo.apply(RegisterUser("first", "last", "email@com", encryptPassword)))
        validatePassword <- BcryptObject.validatePassword("abc", userInfo)
      yield assertTrue(validatePassword.contains(true))
    },
    test("should validate the incorrect password") {
      for
        encryptPassword <- BcryptObject.encryptPassword("abcd")
        userInfo = Some(UserInfo.apply(RegisterUser("first", "last", "email@com", encryptPassword)))
        validatePassword <- BcryptObject.validatePassword("abc", userInfo)
      yield assertTrue(validatePassword.contains(false))
    }
  )
