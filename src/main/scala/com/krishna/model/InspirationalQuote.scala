package com.krishna.model

import java.time.LocalDate
import java.util.UUID

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

case class InspirationalQuote(
  serialId: UUID,
  quote: Quote,
  author: Option[String],
  relatedInfo: Option[String],
  genre: Set[String],
  storedDate: LocalDate
)

object InspirationalQuote:
  given JsonEncoder[InspirationalQuote] = DeriveJsonEncoder.gen
