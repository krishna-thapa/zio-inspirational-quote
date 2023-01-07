package com.krishna.auth.model

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
