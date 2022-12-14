package com.krishna

import zio.*
import zio.config.*
import zio.config.magnolia.{ Descriptor, descriptor }
import zio.config.typesafe.TypesafeConfigSource

package object configuration:

  type QuoteAndWikiConfig = WikiConfig with QuoteConfig
  type Configuration      = WikiConfig with QuoteConfig with DatabaseConfig

  final case class WikiConfig(apiUrl: String)
  final case class QuoteConfig(csvPath: String, batchSize: Int)

  final case class DatabaseConfig(
    dataSourceClassName: String,
    user: String,
    password: String,
    databaseName: String,
    portNumber: Int,
    serverName: String,
    connectionTimeout: Int
  )

  val wikiConfig: URIO[WikiConfig, WikiConfig]             = ZIO.service[WikiConfig]
  val quoteConfig: URIO[QuoteConfig, QuoteConfig]          = ZIO.service[QuoteConfig]
  val databaseConfig: URIO[DatabaseConfig, DatabaseConfig] = ZIO.service[DatabaseConfig]

  object Configuration:

    def getEnvironmentConfig[T: Tag](
      configPath: String
    )(using Descriptor[T]): ZLayer[Any, ReadError[String], T] =
      ZLayer {
        read {
          descriptor[T].from(
            TypesafeConfigSource
              .fromResourcePath
              .at(PropertyTreePath.$(configPath))
          )
        }
      }

    val layer: Layer[ReadError[String], Configuration] =
      getEnvironmentConfig[WikiConfig]("WikiConfig") ++
        getEnvironmentConfig[QuoteConfig]("QuoteConfig") ++
        getEnvironmentConfig[DatabaseConfig]("DatabaseConfig")
