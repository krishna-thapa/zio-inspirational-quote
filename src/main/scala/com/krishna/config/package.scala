package com.krishna

import zio.*
import zio.config.*
import zio.config.magnolia.{ Descriptor, descriptor }
import zio.config.typesafe.TypesafeConfigSource

package object config:

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

  object DatabaseConfig:
    def validateConfig(dbConfig: DatabaseConfig): Boolean =
      dbConfig.serverName.nonEmpty && dbConfig.user.nonEmpty &&
      dbConfig.password.nonEmpty && dbConfig.databaseName.nonEmpty

  val wikiConfig: URIO[WikiConfig, WikiConfig]             = ZIO.service[WikiConfig]
  val quoteConfig: URIO[QuoteConfig, QuoteConfig]          = ZIO.service[QuoteConfig]
  val databaseConfig: URIO[DatabaseConfig, DatabaseConfig] = ZIO.service[DatabaseConfig]

  object Configuration:

    private def getEnvironmentConfig[T: Tag](
      configPath: String
    )(using Descriptor[T]): Layer[ReadError[String], T] =
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
