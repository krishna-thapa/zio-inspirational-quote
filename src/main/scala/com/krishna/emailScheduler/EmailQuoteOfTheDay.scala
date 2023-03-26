package com.krishna.emailScheduler

import java.nio.charset.Charset

import zio.{ IO, ZIO }

import com.krishna.config.EmailConfig
import com.krishna.model.*
import com.krishna.service.EmailInterface

object EmailQuoteOfTheDay:

  extension [T](content: T) private def some: Option[T] = Some(content)

  /**
   * Sets the mailer settings with envelope as per the system environments parameters from
   *   the user's email and password. It will add list of the emails to the envelope to parameter
   * @param emails List of emails to send the quote of the day as mail body
   * @param body Quote of the day in String format
   * @return Tuple of mailer settings and envelope
   */
  private def zioEmailSettingsWithEnvelope(
    emails: List[String],
    body: String
  ): IO[Throwable, (MailerSettings, Envelope)] =
    for
      emailConfig   <- EmailConfig.getEmailConfig
      validateEmail <- Email.safe(emailConfig.userEmail)
    yield (
      MailerSettings(
        host = emailConfig.host.some,
        port = 465.some,
        creds = Credentials(validateEmail, emailConfig.password).some,
        ssl = true.some,
        auth = true.some
      ),
      Envelope(
        from = emailConfig.userEmail,
        subject = ("Quote of the Day!!", Charset.defaultCharset.some).some,
        to = List("krishna.nature91@gmail.com"),
        content = Text(body)
      )
    )

  /**
   * Sends the email notification to all the registered users with the quote of the day
   * @param emails List of registered user's email
   * @return Unit value
   */
  def sendmailNotification(
    emails: List[String],
    quote: InspirationalQuote
  ): ZIO[EmailInterface, Throwable, Unit] =
    for
      settingsWithEnvelope <- zioEmailSettingsWithEnvelope(emails, quoteTOEmailBody(quote))
      _ <- EmailInterface.sendMail(settingsWithEnvelope._2, settingsWithEnvelope._1)
    yield ()

  private val quoteTOEmailBody: InspirationalQuote => String = quote => s"""
      |${quote.quote.quote}
      |${quote.relatedInfo.map(info => s"Related info: $info").getOrElse("")}
      |${quote.author.map(auth => s"By Author: $auth").getOrElse("")}
      |Genres: ${quote.genre.mkString(", ")}
      |""".stripMargin
