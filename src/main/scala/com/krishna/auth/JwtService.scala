package com.krishna.auth

import java.time.Clock

import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.http.*
import zio.http.model.Status
import zio.json.*
import zio.{ IO, ZIO }

import com.krishna.errorHandle.ErrorHandle
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

  /**
   * Authorize the user by validating the JWT token from the header of the request
   * @param req HTTP Request
   * @param isAdmin Boolean flag to validate if the user of logging in as Admin role or not
   * @return Option of the user details
   */
  def authenticate(req: Request, isAdmin: Boolean = false): Option[JwtUser] =
    req
      .headers
      .get("X-ACCESS-TOKEN")
      .flatMap(token => if isAdmin then isUserAdmin(token) else isUserValid(token))
