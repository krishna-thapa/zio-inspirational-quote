package com.krishna.model.user

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

import com.krishna.model.user.traits.{ Email, UserDetail }

case class JwtUser(firstName: String, lastName: String, email: String, isAdmin: Boolean)
  extends Email
  with UserDetail

object JwtUser:
  given JsonEncoder[JwtUser] = DeriveJsonEncoder.gen[JwtUser]
  given JsonDecoder[JwtUser] = DeriveJsonDecoder.gen[JwtUser]

  def apply(userInfo: UserInfo): JwtUser =
    JwtUser(userInfo.firstName, userInfo.lastName, userInfo.email, userInfo.isAdmin)

  def apply(userRegister: RegisterUser): JwtUser =
    JwtUser(userRegister.firstName, userRegister.lastName, userRegister.email, isAdmin = false)
