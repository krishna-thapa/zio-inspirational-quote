package com.krishna.database

import com.krishna.model.InspirationalQuote
import zio.ZIO
import zio.jdbc.{ SqlFragment, UpdateResult, ZConnectionPool, insert, sqlInterpolator, transaction }

object JdbcQueries:

  private val getConfigValue: String => ZIO[Any, RuntimeException, String] = envVariable =>
    for
      configValueOpt <- zio.System.env(envVariable)
      configValue    <- ZIO
        .fromOption(configValueOpt)
        .orElseFail(new RuntimeException(s"Missing the \"$envVariable\" environment variable."))
    yield configValue

  lazy val quoteTable: ZIO[Any, RuntimeException, String] = getConfigValue("")

  // Inserting from tuples:
  private val insertQuoteSql: (String, InspirationalQuote) => SqlFragment = (table, quote) =>
    sql"insert into $table (name, age) values('John', 42)"

  def insertQuote(
    quote: InspirationalQuote
  ): ZIO[Any, RuntimeException, ZIO[ZConnectionPool, Throwable, UpdateResult]] =
    for tableName <- quoteTable
    yield transaction {
      insert(insertQuoteSql(tableName, quote))
    }
