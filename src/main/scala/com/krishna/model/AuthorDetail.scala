package com.krishna.model

case class AuthorDetail(
  title: String,
  alias: Seq[String] = Seq.empty,
  description: Seq[String] = Seq.empty,
  imagerUrl: Option[String] = None,
)

object AuthorDetail:
  def isEmpty(author: AuthorDetail): Boolean =
    author.title.isEmpty &&
    author.alias.isEmpty &&
    author.description.isEmpty &&
    author.imagerUrl.isEmpty
