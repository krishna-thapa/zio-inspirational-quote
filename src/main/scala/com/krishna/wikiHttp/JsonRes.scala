package com.krishna.wikiHttp

object JsonRes:
  opaque type JsonBody = String

  object JsonBody:
    def apply(value: String): JsonBody = value

  extension (jsonBody: JsonBody) def value: String = jsonBody
