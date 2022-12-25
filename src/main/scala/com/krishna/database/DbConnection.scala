package com.krishna.database

import com.krishna.config.DatabaseConfig
import zio.*
import zio.jdbc.{ ZConnectionPool, ZConnectionPoolConfig }

object DbConnection:

  // With maximum connection of 32
  private val createZIOPoolConfig: ULayer[ZConnectionPoolConfig] =
    ZLayer.succeed(ZConnectionPoolConfig.default)

  val dbPool: URIO[DatabaseConfig, Layer[Throwable, ZConnectionPool]] =
    for
      _        <- ZIO.logInfo("Getting Database connection pool!")
      dbConfig <- com.krishna.config.databaseConfig
      properties = Map(
        "user"     -> "mysql",
        "password" -> "mysql"
      )
    yield createZIOPoolConfig >>>
      ZConnectionPool.postgres("localhost", 3306, "mysql", properties)
