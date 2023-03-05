package com.krishna.http.api.user

import zio.http.*
import zio.http.model.{ Method, Status }
import zio.{ UIO, ZIO }

import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.http.ConfigHttp
import com.krishna.model.user.JwtUser

object UserQuoteHttp:

  // CSVId should start with "CSV" prefix and digit should be greater than 100
  private lazy val csvIdPattern = "CSV(?!(?:\\d{1,2}|100)$)[0-9]\\d+$".r

  private def validateDatabaseResponse(response: Int, service: String): Response =
    if response != 1 then
      Response
        .text(s"Invalid response from the Postgres service while $service")
        .setStatus(Status.InternalServerError)
    else Response.text(s"$service success!!")

  def apply(claim: JwtUser): Http[QuoteRepo with UserRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.POST -> !! / "quote" / "fav" / quoteId =>
        if csvIdPattern.matches(quoteId) then
          ZIO.logInfo(s"Inserting a favourite quote for the user: ${claim.email}") *>
            (for
              userId   <- UserRepo.userInfo(claim.email).map(_.userId)
              response <- QuoteRepo.runUpdateFavQuote(userId, quoteId)
            yield validateDatabaseResponse(response, "updating fav quote"))
        else
          val message: String =
            s"Invalid quote id: $quoteId, have to start with CSV and digit should be greater than 100."
          ZIO
            .logError(message)
            .as(Response.text(message).setStatus(Status.BadRequest))

      // Get all the fav quotes with boolean that can show all the quotes in history that users have liked
      case req @ Method.GET -> !! / "quote" / "fav" / "all" =>
        val getHistoryQuotes: Boolean =
          req.url.queryParams.get("get_history_quotes").exists(_.apply(0).toBoolean)
        ZIO.logInfo(
          s"Getting all favourite quotes of the user: ${claim.email} with history flag: $getHistoryQuotes"
        ) *>
          (for
            userId <- UserRepo.userInfo(claim.email).map(_.userId)
            quotes <- QuoteRepo.runGetAllFavQuotes(userId, getHistoryQuotes)
            _      <- ZIO.logInfo(s"Success on getting all fav quotes for user id $userId")
          yield ConfigHttp.convertToJson(quotes))
      case Method.GET -> !! / "quote" / uuid(uuid)          =>
        ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
          (for
            quote <- QuoteRepo.runSelectQuote(uuid)
            _     <- ZIO.logInfo(s"Success on getting quote with id $uuid")
          yield ConfigHttp.convertToJson(quote))
    }
