package com.krishna.database.quotes

import com.sun.tools.javac.resources.CompilerProperties.Fragments
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.util.fragment
import doobie.util.fragment.Fragment
import zio.*
import zio.interop.catz.*

import com.krishna.config.DatabaseConfig
import com.krishna.database.DbConnection
import com.krishna.model.InspirationalQuote

case class QuoteDbService() extends Persistence:

  def runDoobieTxa(
    updateQuery: doobie.Update0
  ): ZIO[DatabaseConfig, Throwable, Task[Int]] =
    ZIO.scoped {
      for
        txa      <- DbConnection.transactor
        response <- updateQuery.run.transact(txa)
      yield ZIO.attemptBlockingIO(response)
    }

  def insertQuotes(tableName: String, quote: InspirationalQuote): doobie.Update0 =
    val insertToTable  = fr"INSERT INTO " ++
      Fragment.const(tableName) ++
      fr" (serial_id, quote, author, relatedInfo, genre, stored_date) "
    val valuesToInsert =
      fr"VALUES (${quote.serialId}, ${quote.quote.quote}, ${quote.author}, ${quote.relatedInfo}, ${quote.genre.toArray}, ${quote.storedDate})"
    (insertToTable ++ valuesToInsert).update

  def truncateTable(tableName: String): doobie.Update0 =
    (fr"TRUNCATE TABLE " ++ Fragment.const(tableName) ++ fr" CASCADE").update
  
  def runTruncateTable(): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =
    for
      getDbConfig <- com.krishna.config.databaseConfig
      tableName <- DatabaseConfig.validateTable(getDbConfig)
      query = truncateTable(tableName)
      response <- runDoobieTxa(query)
    yield response
    
  def runMigrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =

    for
      getDbConfig <- com.krishna.config.databaseConfig
      tableName <- DatabaseConfig.validateTable(getDbConfig)
      query = insertQuotes(tableName, quote)
      response <- runDoobieTxa(query)
    yield response

object QuoteDbService:

  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
