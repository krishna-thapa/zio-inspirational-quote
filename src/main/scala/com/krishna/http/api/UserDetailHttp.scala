package com.krishna.http.api

import pdi.jwt.JwtClaim
import zio.*
import zio.http.*
import zio.http.model.{ HttpError, Method }
import zio.json.*

import com.krishna.database.user.{ UserRepo, UserService }
import com.krishna.model.user.RegisterUser

object UserDetailHttp:

  def apply(claim: JwtClaim): Http[UserRepo, Throwable, Request, Response] = ???
