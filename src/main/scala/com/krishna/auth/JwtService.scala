package com.krishna.auth

import java.time.Clock

import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import zio.ZIO
import zio.http.model.Status
import zio.http.{ Http, HttpApp, Request }

object JwtService:

  // Secret Authentication key
  val SECRET_KEY: String = "secretKey"

  given Clock = Clock.systemUTC

  // Helper to encode the JWT token
  def jwtEncode(username: String): String =
    val json  = s"""{"user": "${username}"}"""
    val claim = JwtClaim(json).issuedNow.expiresIn(1800)
    Jwt.encode(claim, SECRET_KEY, JwtAlgorithm.HS512)

  // Helper to decode the JWT token
  def jwtDecode(token: String): Option[JwtClaim] =
    Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS512)).toOption

  // Authentication middleware
  // Takes in a Failing HttpApp and a Succeed HttpApp which are called based on Authentication success or failure
  // For each request tries to read the `X-ACCESS-TOKEN` header
  // Validates JWT Claim
  def authenticate[R, E](success: JwtClaim => HttpApp[R, E]): HttpApp[R, E] =
    val fail: HttpApp[Any, Nothing] =
      Http.text("User not allowed!").setStatus(Status.Unauthorized)

    Http
      .fromFunction[Request] {
        _.headers
          .get("X-ACCESS-TOKEN")
          .flatMap(jwtDecode)
          .fold[HttpApp[R, E]](fail)(success)
      }
      .flatten
