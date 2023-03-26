package com.krishna.model

import scala.util.{ Failure, Success, Try }

import zio.{ Task, ZIO }

opaque type Email = String

object Email:
  def apply(email: String): Email = email

  extension (email: Email) def value: String = email

  // TODO used this opaque method for email in rest of the code
  def safe(email: String): Task[Email] =
    ZIO.fromTry(
      if """^[A-Za-z0-9+_.-]+@(.+)$""".r.matches(email) then Success(email)
      else Failure(new Exception(s"Invalid email pattern for email: $email"))
    )
