package com.krishna.util

import java.nio.charset.Charset

import zio.ZIO

import com.krishna.model.{ Credentials, Envelope, MailerSettings, Text }
import com.krishna.service.EmailInterface

object EmailQuoteOfTheDay:

  extension [T](content: T) private def some: Option[T] = Some(content)

  val settings: MailerSettings = MailerSettings(
    host = "smtp.mail.yahoo.com".some,
    port = 465.some,
    creds = Credentials("<Some-thing>", "<Some-thing>").some,
    ssl = true.some,
    auth = true.some
  )

  val e: Envelope = Envelope(
    from = "<Some-thing>",
    subject = ("Some subject", Charset.defaultCharset.some).some,
    to = List("krishna.nature91@gmail.com"),
    content = Text("Nonsense")
  )

  def sendmailNotification: ZIO[EmailInterface, Throwable, Unit] =
    for _ <- EmailInterface.sendMail(e, settings)
    yield ()
