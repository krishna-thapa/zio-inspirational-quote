package com.krishna.http.api.user

import zio.{ UIO, ZIO }
import zio.http.*
import zio.http.model.{ Method, Status }
import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.http.ConfigHttp
import com.krishna.model.user.JwtUser

object UserQuoteHttp:

  // CsvId should start with "CSV" prefix
  private lazy val csvIdPattern = "CSV\\d+$".r
  // if csvIdPattern.matches(str) then str else throw Exception("wrong csv id")

  private def validateDatabaseResponse(response: Int, service: String): Response =
    if response != 1 then
      Response
        .text(s"Invalid response from the Postgres service while $service")
        .setStatus(Status.InternalServerError)
    else Response.text(s"$service success!!")

  def apply(claim: JwtUser): Http[QuoteRepo with UserRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.POST -> !! / "quote" / "fav" / quoteId =>
        ZIO.logInfo(s"Inserting a favourite quote for the user: ${claim.email}") *>
          (for
            userId   <- UserRepo.userInfo(claim.email).map(_.userId)
            response <- QuoteRepo.runUpdateFavQuote(userId, quoteId)
          yield validateDatabaseResponse(response, "updating fav quote"))

      // Get all the fav quotes with boolean that can show all the quotes in history that users have liked
      case req @ Method.GET -> !! / "quote" / "fav" / "all" =>
        val getHistoryQuotes: Boolean =
          req.url.queryParams.get("get_history_quotes").exists(_.apply(0).toBoolean)
        ZIO.logInfo(s"Getting all favourite qoutes of the user: ${claim.email}") *>
          (for
            quote <- QuoteRepo.runSelectQuote(uuid)
            _     <- ZIO.logInfo(s"Success on getting quote with id $uuid")
          yield ConfigHttp.convertToJson(quote))
      case Method.GET -> !! / "quote" / uuid(uuid)          =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
          (for
            quote <- QuoteRepo.runSelectQuote(uuid)
            _     <- ZIO.logInfo(s"Success on getting quote with id $uuid")
          yield ConfigHttp.convertToJson(quote))
    }
