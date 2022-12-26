package com.krishna.database

import zio.*
import zio.jdbc.{ ZConnectionPool, ZConnectionPoolConfig }

import com.krishna.config.DatabaseConfig

object DbConnection:

  // Default with maximum connection of 32
  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  // Create a connection pool with Postgres Database client
  val dbConnectionPool
    : ZIO[DatabaseConfig, Throwable, ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool]] =
    for
      _           <- ZIO.logInfo("Getting Database connection pool!")
      getDbConfig <- com.krishna.config.databaseConfig
      dbConfig    <- DatabaseConfig.validateConfig(getDbConfig)
      properties = Map(
        "user"     -> dbConfig.user,
        "password" -> dbConfig.password
      )
    yield ZConnectionPool.postgres(
      dbConfig.serverName,
      dbConfig.portNumber,
      dbConfig.databaseName,
      properties
    )
