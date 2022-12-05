package com.krishna.config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class WikiConfig(apiUrl: String)
case class EnvironmentConfig(
  csvPath: String,
  wiki: WikiConfig,
  batchSize: Int,
)

object EnvironmentConfig:
  val layer: ZLayer[Any, ReadError[String], EnvironmentConfig] =
    ZLayer {
      read {
        descriptor[EnvironmentConfig].from(
          TypesafeConfigSource
            .fromResourcePath
            .at(PropertyTreePath.$("QuoteConfig"))
        )
      }
    }
