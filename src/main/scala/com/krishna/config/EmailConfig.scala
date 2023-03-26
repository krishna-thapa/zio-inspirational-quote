package com.krishna.config

import zio.config.ReadError
import zio.{ Layer, Task, URIO, ZIO }

final case class EmailConfig(host: String, userEmail: String, password: String)

object EmailConfig:

  private val emailConfig: URIO[EmailConfig, EmailConfig] = ZIO.service[EmailConfig]

  private val emailLayer: Layer[ReadError[String], EmailConfig] =
    Configuration.getEnvironmentConfig[EmailConfig]("EmailConfig")

  val getEmailConfig: Task[EmailConfig] = {
    for getEmailConfig <- emailConfig
    yield getEmailConfig
  }.provide(emailLayer)
