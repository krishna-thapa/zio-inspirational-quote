package com.krishna.csvStore

import com.krishna.config
import com.krishna.config.Configuration
import zio.{ Task, ZIO }

object CsvUtil:

  val quoteConfig: Task[config.QuoteConfig] = {
    for quoteConfig <- com.krishna.config.quoteConfig
    yield quoteConfig
  }.provide(Configuration.quoteLayer)
