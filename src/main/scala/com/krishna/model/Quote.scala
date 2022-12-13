package com.krishna.model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Quote(quote: String)

object Quote:
  given JsonEncoder[Quote] = DeriveJsonEncoder.gen
  given JsonDecoder[Quote] = DeriveJsonDecoder.gen

  def validate(quote: String): Option[Quote] =
    if quote.nonEmpty then Some(Quote(quote)) else None
