package com.krishna.model

import java.util.UUID

import com.krishna.model.Quotes.*

case class InspirationalQuote(
  serialId: UUID,
  quote: Quote,
  author: Option[AuthorDetail],
  genre: Set[String]
)
