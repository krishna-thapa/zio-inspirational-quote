package com.krishna.model

case class AuthorDetail(
  title: String,
  alias: Seq[String] = Seq.empty,
  description: Seq[String] = Seq.empty,
  imagerUrl: Option[String] = None,
)
