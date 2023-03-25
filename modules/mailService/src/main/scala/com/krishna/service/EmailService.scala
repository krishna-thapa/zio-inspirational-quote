package com.krishna.service

import zio.{ Task, ULayer, ZLayer }

import com.krishna.model.{ Envelope, MailerSettings }

class EmailService extends EmailInterface:

  override def sendMail(envelope: Envelope, settings: MailerSettings): Task[Unit] =
    for _ <-
        MailSession.sendMail(envelope, settings)
    yield ()

object EmailService:
  val layer: ULayer[EmailService] = ZLayer.succeed(EmailService())
