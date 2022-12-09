package com.krishna.model

import java.util.UUID

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

case class InspirationalQuote(
  serialId: UUID,
  quote: Quote,
  author: Option[AuthorDetail],
  genre: Set[String])

object InspirationalQuote:
  given JsonEncoder[InspirationalQuote] = DeriveJsonEncoder.gen
