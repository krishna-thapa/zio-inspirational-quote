package com.krishna.model

import java.nio.charset.Charset

sealed trait Content

final case class Text(
  body: String,
  // TODO read more here: https://www.baeldung.com/java-char-encoding
  charset: Charset = Charset.defaultCharset() // UTF-8
) extends Content
