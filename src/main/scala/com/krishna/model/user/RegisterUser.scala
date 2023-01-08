package com.krishna.model.user

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

case class RegisterUser(
  firstName: String,
  lastName: String,
  email: String,
  password: String
) extends Email

object RegisterUser:
  given JsonEncoder[RegisterUser] = DeriveJsonEncoder.gen[RegisterUser]

  given JsonDecoder[RegisterUser] = DeriveJsonDecoder.gen[RegisterUser]

  def validateForm(form: RegisterUser): Boolean =
    if form.firstName.nonEmpty &&
      form.lastName.nonEmpty &&
      Email.validEmail(form.email) &&
      (form.password.length >= 3)
    then true
    else false
