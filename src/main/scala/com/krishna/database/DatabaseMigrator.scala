package com.krishna.database

import org.flywaydb.core.Flyway
import zio.*

import com.krishna.config.DatabaseConfig

object DatabaseMigrator:

  def migrate: ZIO[DatabaseConfig, Throwable, Unit] =
    for
      _           <- ZIO.logInfo("Running flyway Database migration!!")
      getDbConfig <- com.krishna.config.databaseConfig
      dbConfig    <- DatabaseConfig.validateConfig(getDbConfig)
      flyway      <- ZIO.attempt(
        Flyway
          .configure()
          .validateMigrationNaming(true)
          .baselineOnMigrate(true)
          .dataSource(
            s"jdbc:postgresql://${dbConfig.serverName}:${dbConfig.portNumber}/${dbConfig.databaseName}",
            dbConfig.user,
            dbConfig.password
          )
          .load()
      )
      _           <- ZIO.attempt {
        // flyway.clean()
        flyway.migrate()
      }
      _           <- ZIO.logInfo("Successfully ran flyway Database migration!!")
    yield ()
