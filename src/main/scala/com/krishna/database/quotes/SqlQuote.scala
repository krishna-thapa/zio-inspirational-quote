package com.krishna.database.quotes

import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import zio.interop.catz.*
import zio.{ Task, ZIO }

import com.krishna.config.DatabaseConfig
import com.krishna.database.DbConnection
import com.krishna.model.InspirationalQuote

object SqlQuote:

  def runDoobieTxa(
    updateQuery: doobie.Update0
  ): ZIO[DatabaseConfig, Throwable, Task[Int]] =
    ZIO.scoped {
      for
        txa      <- DbConnection.transactor
        response <- updateQuery.run.transact(txa)
      yield ZIO.attemptBlockingIO(response)
    }

  val truncateTable: String => doobie.Update0 = tableName =>
    (fr"TRUNCATE TABLE " ++ Fragment.const(tableName) ++ fr" CASCADE").update

  val insertQuote: (String, InspirationalQuote) => doobie.Update0 = (tableName, quote) =>
    val insertToTable  = fr"INSERT INTO " ++
      Fragment.const(tableName) ++
      fr" (serial_id, quote, author, relatedInfo, genre, stored_date) "
    val valuesToInsert =
      fr"VALUES (${quote.serialId}, ${quote.quote.quote}, ${quote.author}, ${quote.relatedInfo}, ${quote.genre.toArray}, ${quote.storedDate})"
    (insertToTable ++ valuesToInsert).update
