package com.krishna.database

import com.krishna.config.DatabaseConfig
import org.flywaydb.core.Flyway
import zio.*

object DatabaseMigrator:

  private val validateDbConfig = (dbConfig: DatabaseConfig) =>
    if DatabaseConfig.validateConfig(dbConfig) then ZIO.succeed(dbConfig)
    else ZIO.fail(new RuntimeException(s"Missing the Database configuration environment variables."))

  def migrate: ZIO[DatabaseConfig, Throwable, Unit] =
    for
      _           <- ZIO.logInfo("Running flyway Database migration!!")
      getDbConfig <- com.krishna.config.databaseConfig
      dbConfig    <- validateDbConfig(getDbConfig)
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
    yield ()
