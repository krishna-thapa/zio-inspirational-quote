package com.krishna.util

import java.util.UUID

import cats.effect.IO
import doobie.implicits.*
import doobie.postgres.implicits.*
import zio.config.ReadError
import zio.interop.catz.*
import zio.{ Task, ZIO }

import com.krishna.config.{ Configuration, DatabaseConfig }
import com.krishna.database.DbConnection

object SqlCommon:

  def runUpdateTxa(
    updateQuery: doobie.Update0
  ): Task[Int] =
    ZIO
      .scoped {
        for
          txa      <- DbConnection.transactor
          runQuery <- updateQuery.run.transact(txa)
          response <- ZIO.attemptBlockingIO(runQuery)
        yield response
      }
      .provide(Configuration.databaseLayer)

  def runQueryTxa[T](
    getQuery: doobie.Query0[T]
  ): Task[List[T]] =
    ZIO
      .scoped {
        for
          txa      <- DbConnection.transactor
          runQuery <- getQuery.to[List].transact(txa)
          response <- ZIO.attemptBlockingIO(runQuery)
        yield response
      }
      .provide(Configuration.databaseLayer)

  def runQueryTxa[T](
    getQuery: doobie.ConnectionIO[T]
  ): Task[T] =
    ZIO
      .scoped {
        for
          txa      <- DbConnection.transactor
          runQuery <- getQuery.transact(txa)
          response <- ZIO.attemptBlockingIO(runQuery)
        yield response
      }
      .provide(Configuration.databaseLayer)
