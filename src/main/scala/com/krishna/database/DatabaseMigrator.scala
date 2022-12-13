package com.krishna.database

import org.flywaydb.core.Flyway
import zio.ZIO

object DatabaseMigrator:

  val getConfigValue = (envVariable: String) =>
    for
      configValueOpt <- zio.System.env(envVariable).orDie
      configValue    <- ZIO
        .fromOption(configValueOpt)
        .orElseFail(new RuntimeException(s"Missing the \"$envVariable\" environment variable."))
    yield configValue

  def migrate: ZIO[Any, Throwable, Unit] =
    for
      _          <- ZIO.logInfo("Running flyway Database migration!!")
      hostDb     <- getConfigValue("POSTGRES_SERVER")
      portDb     <- getConfigValue("POSTGRES_PORT")
      database   <- getConfigValue("POSTGRES_DB")
      userDb     <- getConfigValue("POSTGRES_USER")
      passwordDb <- getConfigValue("POSTGRES_PASSWORD")
      flyway     <- ZIO.attempt(
        Flyway
          .configure()
          .validateMigrationNaming(true)
          .baselineOnMigrate(true)
          .dataSource(s"jdbc:postgresql://$hostDb:$portDb/$database", userDb, passwordDb)
          .load()
      )
      _          <- ZIO.attempt {
        // flyway.clean()
        flyway.migrate()
      }
    yield ()
