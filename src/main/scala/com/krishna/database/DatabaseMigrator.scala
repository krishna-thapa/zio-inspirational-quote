package com.krishna.database

import org.flywaydb.core.Flyway
import zio.*

import com.krishna.config
import com.krishna.config.{ Configuration, DatabaseConfig }

object DatabaseMigrator:

  val dbConfig: Task[config.DatabaseConfig] = {
    for quoteConfig <- com.krishna.config.databaseConfig
    yield quoteConfig
  }.provide(Configuration.databaseLayer)

  /** Use of flyway Database migration to migrate the SQL queries. It will always migrate when the
    * project is ran but only will apply if there are new changes.
    * @see
    *   <a href="https://flywaydb.org/documentation/concepts/migrations.html">More on flyway
    *   migration</a>
    * @return
    *   Success or failure of the Database migration
    */
  def migrate: Task[Unit] =
    for
      _           <- ZIO.logInfo("Running flyway Database migration!!")
      getDbConfig <- dbConfig
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
