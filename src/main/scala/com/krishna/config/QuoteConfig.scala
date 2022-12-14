package com.krishna.config

import zio.*
import zio.config.*
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfigSource

case class WikiConfig(apiUrl: String)
case class QuoteConfig(csvPath: String, wiki: WikiConfig, batchSize: Int)

object QuoteConfig extends EnvironmentConfig:
  override val configPath: String = "QuoteConfig"

  val layer: ZLayer[Any, ReadError[String], QuoteConfig] =
    getEnvironmentConfig[QuoteConfig]
