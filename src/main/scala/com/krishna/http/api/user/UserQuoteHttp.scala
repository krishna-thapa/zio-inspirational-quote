package com.krishna.http.api.user

import zio.ZIO
import zio.http.*
import zio.http.model.Method

import com.krishna.database.quotes.QuoteRepo
import com.krishna.http.ConfigHttp
import com.krishna.model.user.JwtUser

object UserQuoteHttp:

  def apply(claim: JwtUser): Http[QuoteRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "quote" / uuid(uuid) =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
          (for
            quote <- QuoteRepo.runSelectQuote(uuid)
            _     <- ZIO.logInfo(s"Success on getting quote with id $uuid")
          yield ConfigHttp.convertToJson(quote))
    }
