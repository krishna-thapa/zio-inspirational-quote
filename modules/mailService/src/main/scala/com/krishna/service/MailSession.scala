package com.krishna.service

import java.util.Properties
import javax.mail.Message.RecipientType
import javax.mail.internet.{ InternetAddress, MimeMessage }
import javax.mail.{ Authenticator, PasswordAuthentication, Session, Transport }

import zio.{ Task, ZIO }

import com.krishna.model.{ Envelope, MailerSettings, Text }

object MailSession:

  extension (address: String)
    private def toInternetAddress: InternetAddress = new InternetAddress(address)

  private def auth(mailer: MailerSettings): Option[Authenticator] =
    mailer.creds.map { c =>
      new Authenticator:
        override def getPasswordAuthentication: PasswordAuthentication =
          new PasswordAuthentication(c.username, c.password)
    }

  private def getMimeMessage(session: Session): ZIO[Any, Throwable, MimeMessage] =
    for mimeMessage <- ZIO.attempt(new MimeMessage(session))
    yield mimeMessage

  private def getSession(mailer: MailerSettings): ZIO[Any, Throwable, Session] =
    for
      props   <- ZIO.attempt {
        val properties = new Properties(System.getProperties)
        mailer.host.foreach(properties.put("mail.smtp.host", _))
        mailer.port.foreach(p => properties.put("mail.smtp.port", p.toString))
        mailer.auth.foreach(a => properties.put("mail.smtp.auth", a.toString))
        mailer.startTls.foreach(s => properties.put("mail.smtp.starttls.enable", s.toString))
        mailer.ssl.foreach(s => properties.put("mail.smtp.ssl.enable", s.toString))
        mailer.trustAll.collect { case true => properties.put("mail.smtp.ssl.trust", "*") }
        mailer.socketFactory.foreach(s => properties.put("mail.smtp.socketFactory.class", s))
        mailer.socketFactoryPort.foreach(s => properties.put("mail.smtp.socketFactory.port", s))
        properties
      }
      auth    <- ZIO.attempt(auth(mailer))
      session <- ZIO.attempt(Session.getInstance(props, auth.orNull))
    yield session

  private def processEnvelope(e: Envelope, message: MimeMessage): ZIO[Any, Throwable, MimeMessage] =
    for
      _ <- ZIO.attempt(message.addFrom(Array(e.from.toInternetAddress)))
      _ <- ZIO.attempt {
        e.subject match
          case Some((subject, _)) =>
            message.setSubject(subject)
          case None               =>
            message.setSubject("")
      }
      _ <- ZIO.attempt(
        e.to.foreach(x => message.addRecipient(RecipientType.TO, x.toInternetAddress))
      )
      _ <- ZIO.attempt(
        e.cc.foreach(x => message.addRecipient(RecipientType.CC, x.toInternetAddress))
      )
      _ <- ZIO.attempt(
        e.bcc.foreach(x => message.addRecipient(RecipientType.BCC, x.toInternetAddress))
      )
      _ <- ZIO.attempt(e.replyTo.foreach(x => message.setReplyTo(Array(x.toInternetAddress))))
      _ <- ZIO.attempt(e.headers.foreach(x => message.addHeader(x._1, x._2)))
      _ <- ZIO.attempt {
        e.content match
          case Text(t, c) => message.setText(t, c.displayName)
      }
    yield message

  def sendMail(mail: Envelope, settings: MailerSettings): Task[Unit] =
    for
      session     <- getSession(settings)
      mimeMessage <- getMimeMessage(session)
      message     <- processEnvelope(mail, mimeMessage)
      _           <- ZIO.attempt(Transport.send(message))
    yield ()
