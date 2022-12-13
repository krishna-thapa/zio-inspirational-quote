package com.krishna.model

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

final case class AuthorDetail(
  title: AuthorDetail.Title,
  relatedInfo: Option[String],
  alias: Seq[String] = Seq.empty,
  description: Seq[String] = Seq.empty,
  imageUrl: Option[String] = None
)

object AuthorDetail:

  type Title = String

  given JsonEncoder[AuthorDetail] = DeriveJsonEncoder.gen

  def isEmpty(author: AuthorDetail): Boolean =
    author.title.isEmpty &&
    author.alias.isEmpty &&
    author.description.isEmpty &&
    author.imageUrl.isEmpty
