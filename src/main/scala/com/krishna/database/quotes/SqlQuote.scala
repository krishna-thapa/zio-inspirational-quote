package com.krishna.database.quotes

import java.util.UUID

import cats.data.NonEmptyList
import doobie.ConnectionIO
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment
import zio.interop.catz.*
import zio.{ Task, ZIO }

import com.krishna.model.{ FavQuote, InspirationalQuote }

object SqlQuote:

  lazy val truncateTable: String => doobie.Update0 = tableName =>
    (fr"TRUNCATE TABLE " ++ Fragment.const(tableName) ++ fr"CASCADE").update

  lazy val insertQuote: (String, InspirationalQuote) => doobie.Update0 = (tableName, quote) =>
    val insertToTable  = fr"INSERT INTO" ++
      Fragment.const(tableName) ++
      fr"(serial_id, quote, author, related_info, genre, stored_date)"
    val valuesToInsert =
      fr"VALUES (${quote.serialId}, ${quote.quote.quote}, ${quote.author}, ${quote.relatedInfo}, ${quote.genre.toArray}, ${quote.storedDate})"
    (insertToTable ++ valuesToInsert).update

  lazy val selectQuoteColumns: Fragment =
    fr"SELECT serial_id, quote, author, related_info, genre, stored_date from"

  lazy val countRows: String => Fragment = tableName =>
    fr"SELECT COUNT (*) from" ++ Fragment.const(tableName)

  lazy val getAllQuotes: (String, Int, Int) => doobie.Query0[InspirationalQuote] =
    (tableName, offset, limit) =>
      val getQuotes =
        selectQuoteColumns ++ Fragment.const(tableName) ++
          fr"ORDER BY csv_id LIMIT $limit OFFSET $offset"
      getQuotes
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)

  lazy val getRandomQuote: (String, Int) => ConnectionIO[NonEmptyList[InspirationalQuote]] =
    (tableName, rows) =>
      (selectQuoteColumns ++ Fragment.const(tableName) ++ fr"OFFSET floor(random() * (" ++
        countRows(tableName) ++ fr")) LIMIT $rows")
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)
        .nel

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

  lazy val getQuoteBySearchedText: (String, String) => doobie.Query0[InspirationalQuote] =
    (tableName, searchInput) =>
      (fr"SELECT serial_id, quote, author, related_info, genre, stored_date, ts_rank(quote_tsv, query) AS rank from"
        ++ Fragment.const(tableName) ++ fr", to_tsquery('english', $searchInput) query" ++
        fr"WHERE query @@ quote_tsv order by rank DESC limit 5")
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)

  lazy val getGenreTitles: (String, String) => doobie.Query0[Option[String]] =
    (tableName, term) =>
      (fr"SELECT DISTINCT g from" ++ Fragment.const(tableName) ++
        fr"i, unnest(genre) g WHERE lower (g) LIKE ${term.toLowerCase + "%"}")
        .query[Option[String]]

  lazy val isFavRecordExist: (String, UUID, String) => doobie.ConnectionIO[Option[FavQuote]] =
    (tableName, userId, quoteId) =>
      (fr"SELECT * FROM" ++ Fragment.const(
        tableName
      ) ++ fr"WHERE user_id=$userId AND csv_id=$quoteId")
        .query[FavQuote]
        .option

  lazy val insertFavQuoteRow: (String, UUID, String) => doobie.Update0 =
    (tableName, userId, quoteId) =>
      (fr"INSERT INTO" ++ Fragment.const(
        tableName
      ) ++ fr"(user_id, csv_id)" ++
        fr"VALUES ($userId, $quoteId)").update

  lazy val alterFavQuoteRow: (String, Int) => doobie.Update0 =
    (tableName, favRowId) =>
      (fr"UPDATE" ++ Fragment.const(
        tableName
      ) ++ fr"SET fav_tag = NOT fav_tag WHERE id = $favRowId").update

  lazy val getAllFavQuotes
    : (String, String, UUID, Boolean) => doobie.util.query.Query0[InspirationalQuote] =
    (quoteTable, favQuoteTable, userId, historyQuotes) =>
      val userFavQuery: Fragment =
        fr"SELECT csv_id from" ++ Fragment.const(favQuoteTable) ++ fr"WHERE user_id=$userId"
      val innerQuery: Fragment   =
        if !historyQuotes then userFavQuery ++ fr"AND fav_tag" else userFavQuery
      (selectQuoteColumns ++ Fragment.const(quoteTable) ++
        fr"WHERE csv_id IN (" ++ innerQuery ++ fr")")
        .query[(String, String, Option[String], Option[String], List[String], String)]
        .map(InspirationalQuote.rowToQuote)

  // ============================ TODO fix this =====================================
  import zio.stream.ZStream
  import zio.stream.interop.fs2z.*

  lazy val getAuthorWithId = (tableName: String) =>
    (fr"SELECT serial_id, author from" ++ Fragment.const(tableName))
      .query[(UUID, Option[String])]
      .stream
