package com.krishna.model.user

import java.time.LocalDate
import java.util.UUID

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

import com.krishna.model.user.traits.{ Email, UserDetail }

case class UserInfo(
  userId: UUID,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  createdDate: LocalDate,
  isAdmin: Boolean = false,
  isNotification: Boolean = true
) extends Email
  with UserDetail

object UserInfo:

  given JsonEncoder[UserInfo] = DeriveJsonEncoder.gen[UserInfo]

  given JsonDecoder[UserInfo] = DeriveJsonDecoder.gen[UserInfo]

  def apply(form: RegisterUser): UserInfo =
    UserInfo(
      UUID.randomUUID(),
      form.firstName,
      form.lastName,
      form.email,
      form.password,
      LocalDate.now(),
      isNotification = form.isNotification
    )
