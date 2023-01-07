package com.krishna.util

import zio.{ Task, ZIO }

import com.krishna.config.{ Configuration, DatabaseConfig }

object DbUtils:

  /** Validates the table name that is coming from the config file
    */
  val validateDbTable: Task[String] = {
    for
      getDbConfig <- com.krishna.config.databaseConfig
      tableName   <- DatabaseConfig.validateTable(getDbConfig)
    yield tableName
  }.provide(Configuration.databaseLayer)
