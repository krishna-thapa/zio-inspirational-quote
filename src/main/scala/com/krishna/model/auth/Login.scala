package com.krishna.model.auth

import zio.json.*

case class Login(
  userName: String,
  password: String
)

object Login:
  given JsonEncoder[Login] = DeriveJsonEncoder.gen[Login]
  given JsonDecoder[Login] = DeriveJsonDecoder.gen[Login]
