package com.krishna.database

import zio.ZIO
import zio.jdbc.*

import com.krishna.config.DatabaseConfig
import com.krishna.model.InspirationalQuote

object JdbcQueries:

  // Inserting from tuples:
  private val insertQuoteSql: (String, InspirationalQuote) => SqlFragment = (table, quote) =>
    sql"insert into inspirational_quotes (serial_id, quote, stored_date) values(gen_random_uuid (), 'Don', '2017-04-30')"

  def insertQuote(
    quote: InspirationalQuote
  ): ZIO[DatabaseConfig, RuntimeException, ZIO[ZConnectionPool, Throwable, UpdateResult]] =
    for dbConfig <- com.krishna.config.databaseConfig // Validate the Config
    yield transaction {
      insert(insertQuoteSql(dbConfig.tables.quotesTable, quote))
    }
