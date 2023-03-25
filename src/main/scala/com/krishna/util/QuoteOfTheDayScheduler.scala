package com.krishna.util

import java.time.OffsetDateTime

import zio.Schedule.WithState
import zio.http.Response
import zio.{ IO, Schedule, ZIO }

import com.krishna.database.quotes.QuoteRepo
import com.krishna.http.ConfigHttp
import com.krishna.service.EmailInterface

object QuoteOfTheDayScheduler:

  // TODO: Update to Schedule.hourOfDay(2)
  private val scheduler: WithState[(OffsetDateTime, Long), Any, Any, Long] =
    Schedule.minuteOfHour(9)

  /*
  private val orElse: (Throwable, Option[Long]) => IO[RuntimeException, Nothing] =
    (error, _) =>
      ZIO.fail(new RuntimeException(s"Failed while getting quote of the day with error: $error"))
   */

  /** Runs the cron job like scheduler for the quote of the day. It should run every morning at 3
    * am. It will fetch the new quote of the day and store in the redis storage. The quote stored in
    * the Redis will last for one one day and hence it will always be a new quote for the next day.
    * If there is error, it will fail but the project will keep on running. Once the fix is made,
    * the project have to restart to continue the cron job scheduler.
    */
  def getQuoteOfTheDay: ZIO[EmailInterface with QuoteRepo, Throwable, AnyVal] =
    val quoteOfTheDay: ZIO[EmailInterface with QuoteRepo, Throwable, Response] =
      ZIO.logInfo(s"Getting quote of the day for today's, running every day at 2 am!") *>
        (for
          quote <- QuoteRepo.runQuoteOfTheDayQuote()
          _     <- ZIO.logInfo(
            s"Success on getting quote of the day using scheduler cron, quote id: ${quote.serialId}!"
          )
          _     <- EmailQuoteOfTheDay.sendmailNotification
        yield ConfigHttp.convertToJson(quote))

    quoteOfTheDay
      .repeat(scheduler)
      .catchAll(err =>
        ZIO.logWarning(
          s"Recovering while getting quote of the day using the cron job!, from the error: ${err.getMessage}"
        )
      )
