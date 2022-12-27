package com.krishna.database

import com.krishna.config.DatabaseConfig
import com.krishna.model.InspirationalQuote

import zio.ZIO
import zio.jdbc.*

object JdbcQueries:

  // Inserting from tuples:
  private val insertQuoteSql: (String, InspirationalQuote) => SqlFragment = (table, quote) =>
//    val quoteValues = (quote.quote.quote, quote.author, quote.relatedInfo, quote.storedDate.toString)
//    val bar         = Sql
//      .insertInto(table)("quote", "author", "relatedInfo", "stored_date")
//      .values(quoteValues)
    // TODO: Wait until the ZIO JDBC is released and has more supports for types like Array, UUID, Date and JSON
    val str: String = s"insert into inspirational_quotes (quote, stored_date) values('I was eating rice and soup.', ${quote.storedDate.toString})"
    val foo         =
      sql"$str"
    println(s"------------- ${foo.toString}")
    foo

  def insertQuote(
    quote: InspirationalQuote
  ): ZIO[DatabaseConfig, Throwable, ZIO[ZConnectionPool, Throwable, UpdateResult]] =
    for
      dbConfig  <- com.krishna.config.databaseConfig
      tableName <- DatabaseConfig.validateTable(dbConfig)
    yield transaction {
      insert(insertQuoteSql(tableName, quote))
    }
