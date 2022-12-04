package com.krishna.model

import com.krishna.model.Quotes.*

import java.util.UUID

case class InspirationalQuote(
  serialId: UUID,
  quote: Quote,
  author: Option[String],
  genre: Set[String],
)
