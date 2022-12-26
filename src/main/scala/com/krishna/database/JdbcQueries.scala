package com.krishna.database

import com.krishna.config.DatabaseConfig
import com.krishna.model.InspirationalQuote

import zio.ZIO
import zio.jdbc.*

object JdbcQueries:

  // Inserting from tuples:
  private val insertQuoteSql: (String, InspirationalQuote) => SqlFragment = (table, quote) =>
    sql"insert into $table (serial_id, quote, stored_date) values(gen_random_uuid (), 'Don', '2017-04-30')"

  def insertQuote(
    quote: InspirationalQuote
  ): ZIO[DatabaseConfig, Throwable, ZIO[ZConnectionPool, Throwable, UpdateResult]] =
    for
      dbConfig  <- com.krishna.config.databaseConfig
      tableName <- DatabaseConfig.validateTable(dbConfig)
    yield transaction {
      insert(insertQuoteSql(tableName, quote))
    }
