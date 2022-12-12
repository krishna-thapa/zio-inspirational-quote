package com.krishna.main

import zio.*
import io.getquill._
import io.getquill.jdbczio.Quill
import java.sql.SQLException
import com.krishna.model.InspirationalQuote

object JdbcMainApp extends ZIOAppDefault:

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    DataService
      .getPeople
      .provide(
        DataService.live,
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("myDatabaseConfig")
      )
      .debug("Results")
      .exitCode
