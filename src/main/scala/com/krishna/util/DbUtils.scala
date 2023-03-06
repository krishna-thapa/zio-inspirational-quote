package com.krishna.util

import zio.{ Task, ZIO }

import com.krishna.config.{ Configuration, DatabaseConfig, RedisConfig, Tables }

object DbUtils:

  /** Validates the table name that is coming from the config file
    */
  private val validateDbTable: Task[Tables] = {
    for
      getDbConfig <- com.krishna.config.databaseConfig
      tables      <- DatabaseConfig.validateTable(getDbConfig)
    yield tables
  }.provide(Configuration.databaseLayer)

  val getRedisConfig: Task[RedisConfig] = {
    for getRedisConfig <- com.krishna.config.redisConfig
    yield getRedisConfig
  }.provide(Configuration.redisLayer)

  val getQuoteTable: Task[String] =
    for validateTables <- validateDbTable
    yield validateTables.quotesTable

  val getFavTable: Task[String] =
    for validateTables <- validateDbTable
    yield validateTables.userFavTable

  val getUserTable: Task[String] =
    for validateTables <- validateDbTable
    yield validateTables.userTable
