package com.krishna.database

import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import zio.*
import zio.interop.catz.*

import com.krishna.config.{ DatabaseConfig, databaseConfig }

object DbConnection:

  given zioRuntime: zio.Runtime[Any] = zio.Runtime.default

  // To create a Transactor, we need to create an instance of Dispatcher for zio.Task.
  given cats.effect.std.Dispatcher[zio.Task] =
    Unsafe.unsafely {
      zioRuntime
        .unsafe
        .run(
          cats.effect.std.Dispatcher[zio.Task].allocated
        )
        .getOrThrowFiberFailure()
        ._1
    }

  /** Create a connection pool with Postgres Database client. Use of `zio.interop.catz` to Convert a
    * cats Resource into a scoped ZIO.
    */
  lazy val transactor: ZIO[DatabaseConfig with Scope, Throwable, HikariTransactor[Task]] =
    for
      _           <- ZIO.logInfo("Getting Database connection pool!")
      getDbConfig <- com.krishna.config.databaseConfig
      dbConfig    <- DatabaseConfig.validateConfig(getDbConfig)
      ce          <- ExecutionContexts.fixedThreadPool[Task](10).toScopedZIO
      xa          <- HikariTransactor
        .newHikariTransactor[Task](
          driverClassName = "org.postgresql.Driver",
          url =
            s"jdbc:postgresql://${dbConfig.serverName}:${dbConfig.portNumber}/${dbConfig.databaseName}",
          user = dbConfig.user,
          pass = dbConfig.password,
          connectEC = ce
        )
        .toScopedZIO
    yield xa
