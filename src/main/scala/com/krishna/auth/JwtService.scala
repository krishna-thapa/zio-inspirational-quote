package com.krishna.auth

import java.time.Clock

import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.ZIO
import zio.http.model.Status
import zio.http.{ Http, HttpApp, Request }
import zio.json.*

import com.krishna.model.user.JwtUser

object JwtService:

  // Secret Authentication key
  val SECRET_KEY: String = "secretKey"

  given Clock = Clock.systemUTC

  // Helper to encode the JWT token
  def jwtEncode(jwtUser: JwtUser): String =
    val claim = JwtClaim(jwtUser.toJson).issuedNow.expiresIn(1800)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] =
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption

  // TODO: Renew the jWT token whenever there is call to this method
  def isUserValid(token: String): Option[JwtUser] =
    jwtDecode(token).flatMap(_.content.fromJson[JwtUser].toOption)

  def isUserAdmin(token: String): Option[JwtUser] =
    isUserValid(token).filter(_.isAdmin)

  // Takes in a Failing HttpApp and a Succeed HttpApp which are called based on Authentication success or failure
  // For each request tries to read the `X-ACCESS-TOKEN` header
  // Validates JWT Claim
  def authenticateUser[R, E](
    success: JwtUser => HttpApp[R, E],
    isAdmin: Boolean = false
  ): HttpApp[R, E] =
    val fail: HttpApp[Any, Nothing] =
      Http.text("User not allowed!").setStatus(Status.Unauthorized)

    Http
      .fromFunction[Request] {
        _.headers
          .get("X-ACCESS-TOKEN")
          .flatMap(token => if isAdmin then isUserAdmin(token) else isUserValid(token))
          .fold[HttpApp[R, E]](fail)(success)
      }
      .flatten
