package com.krishna.model.user

import java.time.LocalDate
import java.util.UUID

case class UserInfo(
  userId: UUID,
  firstName: String,
  lastName: String,
  email: String,
  password: String,
  createdDate: LocalDate,
  isAdmin: Boolean = false
)

object UserInfo:

  def apply(form: RegisterUser): UserInfo =
    UserInfo(
      UUID.randomUUID(),
      form.firstName,
      form.lastName,
      form.email,
      form.password,
      LocalDate.now()
    )
