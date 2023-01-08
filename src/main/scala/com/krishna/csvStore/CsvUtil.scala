package com.krishna.csvStore

import zio.{ Task, ZIO }

import com.krishna.config
import com.krishna.config.Configuration

object CsvUtil:

  val quoteConfig: Task[config.QuoteConfig] = {
    for quoteConfig <- com.krishna.config.quoteConfig
    yield quoteConfig
  }.provide(Configuration.quoteLayer)
