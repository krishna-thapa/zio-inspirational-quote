package com.krishna.model

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

case class Quote(quote: String)

object Quote:
  given JsonEncoder[Quote] = DeriveJsonEncoder.gen

  def validate(quote: String): Option[Quote] =
    if (quote.nonEmpty) Some(Quote(quote)) else None
