package com.krishna.http

import zhttp.http.*
import zio.*

object VerboseLog:

  /**
   * Generic method to Console print the request and response on each HTTP API call
   */
  def log[R, E >: Throwable]: Middleware[R, E, Request, Response, Request, Response] =
    new Middleware[R, E, Request, Response, Request, Response]:

      override def apply[R1 <: R, E1 >: E](
        http: Http[R1, E1, Request, Response]
      ): Http[R1, E1, Request, Response] =
        http
          .contramapZIO[R1, E1, Request] { req =>
            for
              _ <-
                ZIO.logInfo(s">>> ${req.method} ${req.path} ${req.version}")
              _ <- ZIO.foreach(req.headers.toList) { h =>
                ZIO.logInfo(s">>> ${h._1}: ${h._2}")
              }
            yield req
          }
          .mapZIO[R1, E1, Response] { res =>
            for
              _ <- ZIO.logInfo(s"<<< ${res.status}")
              _ <- ZIO.logInfo(s"<<< Code: ${res.status.code}")
              _ <- ZIO.foreach(res.headers.toList) { h =>
                ZIO.logInfo(s"<<< ${h._1}: ${h._2}")
              }
            yield res
          }
