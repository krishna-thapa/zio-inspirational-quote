package com.krishna.emailScheduler

import java.time.OffsetDateTime

import zio.Schedule.WithState
import zio.http.Response
import zio.{ IO, Schedule, ZIO }

import com.krishna.database.quotes.QuoteRepo
import com.krishna.database.user.UserRepo
import com.krishna.emailScheduler.EmailQuoteOfTheDay
import com.krishna.http.ConfigHttp
import com.krishna.service.EmailInterface

object QuoteOfTheDayScheduler:

  // TODO: Update to Schedule.hourOfDay(3)
  /* Runs the cron job like scheduler for the quote of the day. It should run every morning at 3
   * am. It will fetch the new quote of the day and store in the redis storage. The quote stored in
   * the Redis will last for one one day and hence it will always be a new quote for the next day.
   * If there is error, it will fail but the project will keep on running. Once the fix is made,
   * the project have to restart to continue the cron job scheduler.
   */
  private val scheduler: WithState[(OffsetDateTime, Long), Any, Any, Long] =
    Schedule.minuteOfHour(9)

  private type Environments = EmailInterface with QuoteRepo with UserRepo

  /*
  private val orElse: (Throwable, Option[Long]) => IO[RuntimeException, Nothing] =
    (error, _) =>
      ZIO.fail(new RuntimeException(s"Failed while getting quote of the day with error: $error"))
   */

  /**
   * 1. First get the quote of the day which will always be the new quote.
   * 2. Once the quote is fetched, get all the registered user's email address as list of String
   *  -> Only the users that are already registered and is NOT admin and has enabled email notification
   * 3. Send the email notifications with the quote of the day
   */
  def getQuoteOfTheDay: ZIO[Environments, Throwable, AnyVal] =
    val quoteOfTheDay: ZIO[Environments, Throwable, Response] =
      ZIO.logInfo(s"Getting quote of the day for today's, running every day at 2 am!") *>
        (for
          quote  <- QuoteRepo.runQuoteOfTheDayQuote()
          _      <- ZIO.logInfo(
            s"Success on getting quote of the day using scheduler cron, quote id: ${quote.serialId}!"
          )
          emails <- UserRepo.allUserEmails()
          _      <- EmailQuoteOfTheDay.sendmailNotification(emails, quote)
          _      <- ZIO.logInfo(
            s"Success on sending quote of the day as email notification for total users: ${emails.size}."
          )
        yield ConfigHttp.convertToJson(quote))

    quoteOfTheDay
      .repeat(scheduler)
      .catchAll(err =>
        ZIO.logWarning(
          s"Recovering while getting quote of the day using the cron job!, from the error: ${err.getMessage}"
        )
      )
