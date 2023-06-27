package com.krishna.http

import java.util.concurrent.TimeUnit

import zio.http.HttpAppMiddleware.cors
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.Method
import zio.http.{ HttpAppMiddleware, RequestHandlerMiddleware }
import zio.{ Clock, durationInt }

object MiddlewareConfig:
  
  // Added the CORS config to allow the Swagger UI to access the API via Open API Spec
  val configs: CorsConfig =
    CorsConfig(
      allowedOrigins = s => s.contains("localhost:8080"), // Swagger UI container port
      allowedMethods = Some(Set(Method.GET, Method.POST, Method.DELETE, Method.PUT))
    )

  private val serverTime: RequestHandlerMiddleware[Nothing, Any, Nothing, Any] =
    HttpAppMiddleware.patchZIO(_ =>
      for
        currentMilliseconds <- Clock.currentTime(TimeUnit.MILLISECONDS)
        header = zio.http.Patch.addHeader("X-Time", currentMilliseconds.toString)
      yield header,
    )

  val middlewares =
    // print debug info about request and response
    HttpAppMiddleware.debug ++
      // close connection if request takes more than 3 seconds
      HttpAppMiddleware.timeout(10.seconds) ++
      // add static header
      HttpAppMiddleware.addHeader("X-Environment", "Dev") ++
      // add dynamic header
      serverTime
