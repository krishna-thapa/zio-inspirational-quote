package com.krishna.model.user.traits

trait Email:

  def email: String

object Email:
  val validEmail: String => Boolean = email => """^[A-Za-z0-9+_.-]+@(.+)$""".r.matches(email)
