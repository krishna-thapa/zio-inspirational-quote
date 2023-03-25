package com.krishna.service

import zio.{ Task, ZIO }

import com.krishna.model.{ Envelope, MailerSettings }

trait EmailInterface:
  def sendMail(envelope: Envelope, settings: MailerSettings): Task[Unit]

object EmailInterface:

  def sendMail(envelope: Envelope, settings: MailerSettings): ZIO[EmailInterface, Throwable, Unit] =
    ZIO.serviceWithZIO[EmailInterface](
      _.sendMail(envelope, settings).foldZIO(
        err =>
          ZIO.logError(
            s"Error during sending mail for the user: ${envelope.to}, error message: ${err.getMessage}"
          ) *>
            ZIO.fail(err),
        _ => ZIO.logInfo("Success on sending email about the new quote of the day!")
      )
    )
