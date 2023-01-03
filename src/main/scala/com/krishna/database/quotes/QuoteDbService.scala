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

//  lazy val transactor: ZIO[DatabaseConfig, Throwable, HikariTransactor[Task]] =
//    DbConnection.transactor

  def migrateQuote(quote: InspirationalQuote): ZIO[DatabaseConfig, Throwable, Task[RuntimeFlags]] =

    // .foldM(error => zio.Task.fail(err), _ => Task.succees
    val migrateQuery: String => doobie.Update0 = tableName =>
      val insertToTable  = fr"INSERT INTO " ++ Fragment.const(
        tableName
      ) ++ fr" (serial_id, quote, author, relatedInfo, genre, stored_date) "
      val valuesToInsert =
        fr"VALUES (${quote.serialId}, ${quote.quote.quote}, ${quote.author}, ${quote.relatedInfo}, ${quote.genre.toArray}, ${quote.storedDate})"
      (insertToTable ++ valuesToInsert).update

    for
      txa         <- DbConnection.transactor
      getDbConfig <- com.krishna.config.databaseConfig
      tableName   <- DatabaseConfig.validateTable(getDbConfig)
    yield migrateQuery(tableName).run.transact(txa)

object QuoteDbService:

  val layer: ULayer[QuoteDbService] = ZLayer.succeed(QuoteDbService())
