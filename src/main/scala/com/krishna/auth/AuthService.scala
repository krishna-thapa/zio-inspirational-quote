package com.krishna.auth

import java.time.Clock

import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.http.{ Http, HttpApp, Request }

object AuthService:

  // Secret Authentication key
  val SECRET_KEY: String = "secretKey"

  given Clock = Clock.systemUTC

  // Helper to encode the JWT token
  def jwtEncode(username: String): String =
    val json  = s"""{"user": "${username}"}"""
    val claim = JwtClaim(json).issuedNow.expiresIn(60)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] =
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption

  // Authentication middleware
  // Takes in a Failing HttpApp and a Succeed HttpApp which are called based on Authentication success or failure
  // For each request tries to read the `X-ACCESS-TOKEN` header
  // Validates JWT Claim
  def authenticate[R, E](fail: HttpApp[R, E], success: JwtClaim => HttpApp[R, E]): HttpApp[R, E] =
    Http
      .fromFunction[Request] {
        _.headers
          .get("X-ACCESS-TOKEN")
          .flatMap(header => jwtDecode(header))
          .fold[HttpApp[R, E]](fail)(success)
      }
      .flatten
