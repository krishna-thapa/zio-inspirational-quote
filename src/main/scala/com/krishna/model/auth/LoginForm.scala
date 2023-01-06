package com.krishna.model.auth

import zio.json.*

case class LoginForm(
  userName: String,
  password: String
)

object LoginForm:
  given JsonEncoder[LoginForm] = DeriveJsonEncoder.gen[LoginForm]
  given JsonDecoder[LoginForm] = DeriveJsonDecoder.gen[LoginForm]
