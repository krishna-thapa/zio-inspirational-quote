package com.krishna.database.quotes

import cats.data.NonEmptyList
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import zio.interop.catz.*
import zio.{ Task, ZIO }
import com.krishna.config.DatabaseConfig
import com.krishna.database.DbConnection
import com.krishna.model.InspirationalQuote
import doobie.ConnectionIO

import java.util.UUID

object SqlQuote:

  def runUpdateTxa(
    updateQuery: doobie.Update0
  ): ZIO[DatabaseConfig, Throwable, Task[Int]] =
    ZIO.scoped {
      for
        txa      <- DbConnection.transactor
        response <- updateQuery.run.transact(txa)
      yield ZIO.attemptBlockingIO(response)
    }

  def runQueryTxa[T](
    getQuery: doobie.Query0[T]
  ): ZIO[DatabaseConfig, Throwable, Task[List[T]]] =
    ZIO.scoped {
      for
        txa      <- DbConnection.transactor
        response <- getQuery.to[List].transact(txa)
      yield ZIO.attemptBlockingIO(response)
    }

  def runQueryTxa[T](
    getQuery: doobie.ConnectionIO[T]
  ): ZIO[DatabaseConfig, Throwable, Task[T]] =
    ZIO.scoped {
      for
        txa      <- DbConnection.transactor
        response <- getQuery.transact(txa)
      yield ZIO.attemptBlockingIO(response)
    }

  lazy val truncateTable: String => doobie.Update0 = tableName =>
    (fr"TRUNCATE TABLE " ++ Fragment.const(tableName) ++ fr"CASCADE").update

  lazy val insertQuote: (String, InspirationalQuote) => doobie.Update0 = (tableName, quote) =>
    val insertToTable  = fr"INSERT INTO " ++
      Fragment.const(tableName) ++
      fr"(serial_id, quote, author, related_info, genre, stored_date) "
    val valuesToInsert =
      fr"VALUES (${quote.serialId}, ${quote.quote.quote}, ${quote.author}, ${quote.relatedInfo}, ${quote.genre.toArray}, ${quote.storedDate})"
    (insertToTable ++ valuesToInsert).update

  lazy val selectQuoteColumns: Fragment =
    fr"SELECT serial_id, quote, author, related_info, genre, stored_date from "

  lazy val countRows: String => Fragment = tableName =>
    fr"SELECT COUNT (*) from " ++ Fragment.const(tableName)

  lazy val getAllQuotes: (String, Int, Int) => doobie.Query0[InspirationalQuote] =
    (tableName, offset, limit) =>
      val getQuotes =
        selectQuoteColumns ++ Fragment.const(tableName) ++
          fr"ORDER BY csv_id LIMIT $limit OFFSET $offset"
      getQuotes
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)

  lazy val getRandomQuote: (String, Int) => doobie.Query0[InspirationalQuote] = (tableName, rows) =>
    (selectQuoteColumns ++ Fragment.const(tableName) ++ fr"OFFSET floor(random() * (" ++
      countRows(tableName) ++ fr")) LIMIT $rows")
      .query[(String, String, Option[String], Option[String], List[String], String)]
      .map(InspirationalQuote.rowToQuote)

  lazy val getQuoteById: (String, UUID) => doobie.ConnectionIO[InspirationalQuote] =
    (tableName, uuid) =>
      (selectQuoteColumns ++ Fragment.const(tableName) ++
        fr"WHERE serial_id = $uuid")
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)
        .unique

  lazy val getQuoteByGenre: (String, String) => ConnectionIO[NonEmptyList[InspirationalQuote]] =
    (tableName, genre) =>
      (selectQuoteColumns ++ Fragment.const(tableName) ++
        fr"WHERE $genre = ANY(genre) ORDER BY random() limit 5")
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)
        .nel

  lazy val getGenreTitles: (String, String) => doobie.Query0[Option[String]] =
    (tableName, term) =>
      (fr"SELECT DISTINCT g from" ++ Fragment.const(tableName) ++
        fr"i, unnest(genre) g WHERE lower (g) LIKE ${term.toLowerCase + "%"}")
        .query[Option[String]]
