package com.krishna.model.user

import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

case class RegisterUser(
  firstName: String,
  lastName: String,
  email: String,
  password: String
)

object RegisterUser:
  given JsonEncoder[RegisterUser] = DeriveJsonEncoder.gen[RegisterUser]

  given JsonDecoder[RegisterUser] = DeriveJsonDecoder.gen[RegisterUser]

  private val validEmail: String => Boolean = email =>
    """^[A-Za-z0-9+_.-]+@(.+)$""".r.matches(email)

  def validateForm(form: RegisterUser): Boolean =
    if form.firstName.nonEmpty &&
      form.lastName.nonEmpty &&
      validEmail(form.email) &&
      (form.password.length > 4)
    then true
    else false
