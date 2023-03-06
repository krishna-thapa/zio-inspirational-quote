package com.krishna

import zio.*
import zio.config.*
import zio.config.magnolia.{ Descriptor, descriptor }
import zio.config.typesafe.TypesafeConfigSource

package object config:

  type Configuration = WikiConfig with QuoteConfig with DatabaseConfig

  final case class WikiConfig(apiUrl: String)
  final case class QuoteConfig(csvPath: String, batchSize: Int)
  final case class RedisConfig(hostname: String, database: Int)

  final case class Tables(
    quotesTable: String,
    authorTable: String,
    userTable: String,
    userFavTable: String
  )

  final case class DatabaseConfig(
    dataSourceClassName: String,
    user: String,
    password: String,
    databaseName: String,
    tables: Tables,
    portNumber: Int,
    serverName: String,
    connectionTimeout: Int
  )

  object DatabaseConfig:

    def validateTable(dbConfig: DatabaseConfig): ZIO[Any, RuntimeException, Tables] =
      if dbConfig.tables.quotesTable.nonEmpty && dbConfig.tables.authorTable.nonEmpty &&
        dbConfig.tables.userTable.nonEmpty && dbConfig.tables.userFavTable.nonEmpty
      then ZIO.succeed(dbConfig.tables)
      else
        ZIO.fail(
          new RuntimeException(
            s"Missing the Database configuration environment variables for table names."
          )
        )

    def validateConfig(dbConfig: DatabaseConfig): ZIO[Any, RuntimeException, DatabaseConfig] =
      val isValidate: Boolean = dbConfig.serverName.nonEmpty && dbConfig.user.nonEmpty &&
        dbConfig.password.nonEmpty && dbConfig.databaseName.nonEmpty
      if isValidate then ZIO.succeed(dbConfig)
      else
        ZIO.fail(new RuntimeException(s"Missing the Database configuration environment variables."))

  val wikiConfig: URIO[WikiConfig, WikiConfig]             = ZIO.service[WikiConfig]
  val redisConfig: URIO[RedisConfig, RedisConfig]          = ZIO.service[RedisConfig]
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

    val databaseLayer: Layer[ReadError[String], DatabaseConfig] =
      getEnvironmentConfig[DatabaseConfig]("DatabaseConfig")

    val quoteLayer: Layer[ReadError[String], QuoteConfig] =
      getEnvironmentConfig[QuoteConfig]("QuoteConfig")

    val redisLayer: Layer[ReadError[String], RedisConfig] =
      getEnvironmentConfig[RedisConfig]("RedisConfig")

    val layer: Layer[ReadError[String], Configuration] =
      getEnvironmentConfig[WikiConfig]("WikiConfig") ++
        quoteLayer ++
        databaseLayer
