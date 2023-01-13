package com.krishna.model.user

import zio.json.*

import com.krishna.model.user.traits.Email

case class LoginForm(
  email: String,
  password: String
) extends Email

object LoginForm:
  given JsonEncoder[LoginForm] = DeriveJsonEncoder.gen[LoginForm]
  given JsonDecoder[LoginForm] = DeriveJsonDecoder.gen[LoginForm]

  def validateEmail(email: String): Boolean = Email.validEmail(email)
