package com.krishna.model

import java.time.LocalDate
import java.util.UUID

import doobie.{ Read, Write }
import zio.json.{ DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder }

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

  def rowToQuote(
    row: (String, String, Option[String], Option[String], List[String], String)
  ): InspirationalQuote =
    InspirationalQuote(
      UUID.fromString(row._1),
      Quote(row._2),
      row._3,
      row._4,
      row._5.toSet,
      LocalDate.parse(row._6)
    )
