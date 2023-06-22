package com.krishna.http.api

import zio.http.middleware.Cors.CorsConfig
import zio.http.model.Method

trait CorsConfigQuote:

  // Create CORS configuration
  val config: CorsConfig =
    CorsConfig(
      allowedOrigins = s => s.contains("localhost:8080"),
      allowedMethods = Some(Set(Method.GET))
    )
