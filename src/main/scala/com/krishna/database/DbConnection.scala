package com.krishna.database

import com.krishna.config.DatabaseConfig
import zio.*
import zio.jdbc.{ ZConnectionPool, ZConnectionPoolConfig }

object DbConnection:

  // With maximum connection of 32
  val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val dbPool: URIO[DatabaseConfig, ZLayer[ZConnectionPoolConfig, Throwable, ZConnectionPool]] =
    for
      _        <- ZIO.logInfo("Getting Database connection pool!")
      dbConfig <- com.krishna.config.databaseConfig
      properties = Map(
        "user"     -> "mysql",
        "password" -> "mysql"
      )
    yield ZConnectionPool.postgres("localhost", 3306, "mysql", properties)
