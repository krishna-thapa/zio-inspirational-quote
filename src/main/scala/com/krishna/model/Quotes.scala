package com.krishna.model

object Quotes:

  opaque type Quote = String
  
  object Quote:
    def apply(value: String): Quote = value

  extension (quote: Quote) 
    def validate(value: Quote): Option[Quote] = if (quote.nonEmpty) Some(quote) else None
