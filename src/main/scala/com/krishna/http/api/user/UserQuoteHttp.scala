package com.krishna.http.api.user

import zio.http.*
import zio.http.model.{ Method, Status }
import zio.{ UIO, ZIO }

import com.krishna.auth.JwtService
import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.errorHandle.ErrorHandle
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

  private val internalServerError = (e: Exception) =>
    val message: String = s"Internal server error with message: ${e.getMessage}"
    ZIO
      .logError(message)
      .as(Response.text(message).setStatus(Status.InternalServerError))

  private val emptyResponse: Response =
    Response.text("Empty response from the database!").setStatus(Status.NotFound)

  def apply(): Http[QuoteRepo with UserRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "quote" / "fav" / quoteId =>
        JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
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
        }

      // Get all the fav quotes with boolean that can show all the quotes in history that users have liked
      case req @ Method.GET -> !! / "quote" / "fav" / "all" =>
        JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { claim =>
          val getHistoryQuotes: Boolean =
            req.url.queryParams.get("get_history_quotes").exists(_.apply(0).toBoolean)
          ZIO.logInfo(
            s"Getting all favourite quotes of the user: ${claim.email} with history flag: $getHistoryQuotes"
          ) *>
            (for
              userId <- UserRepo.userInfo(claim.email).map(_.userId)
              quotes <- QuoteRepo.runGetAllFavQuotes(userId, getHistoryQuotes)
              _      <- ZIO.logInfo(s"Success on getting all fav quotes for user id $userId")
            yield if quotes.nonEmpty then ConfigHttp.convertToJson(quotes) else emptyResponse)
              .catchAll {
                case e: Exception => internalServerError(e)
              }
        }

      case req @ Method.GET -> !! / "quote" / uuid(uuid) =>
        JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { _ =>
          ZIO.logInfo(s"Getting a quote from the Postgres database with id $uuid!") *>
            (for
              quote <- QuoteRepo.runSelectQuote(uuid)
              _     <- ZIO.logInfo(s"Success on getting quote with id $uuid")
            yield ConfigHttp.convertToJson(quote)).catchAll {
              case e: Exception => internalServerError(e)
            }
        }

      case req @ Method.GET -> !! / "quote" / "author" / author =>
        JwtService.authenticate(req).fold(ErrorHandle.responseAuthError) { _ =>
          ZIO.logInfo(s"Getting an author details from the Postgres database with for $author") *>
            (for
              author <- QuoteRepo.runGetAuthorDetail(author.trim.replace("%20", " "))
              _      <- ZIO.logInfo(s"Success on getting response for author details: $author")
            yield if author.isDefined then ConfigHttp.convertToJson(author) else emptyResponse)
              .catchAll {
                case e: Exception => internalServerError(e)
              }
        }
    }
