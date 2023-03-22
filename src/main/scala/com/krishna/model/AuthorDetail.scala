package com.krishna.model

import zio.json.{ DeriveJsonEncoder, JsonEncoder }

final case class AuthorDetail(
  title: AuthorDetail.Title,
  alias: Seq[String] = Seq.empty,
  description: Seq[String] = Seq.empty,
  imageUrl: Option[String] = None
)

object AuthorDetail:

  type Title = String

  def rowToAuthor(row: (String, Array[String], Array[String], Option[String])): AuthorDetail =
    AuthorDetail(
      title = row._1,
      alias = row._2.toSeq,
      description = row._3.toSeq,
      imageUrl = row._4
    )

  given JsonEncoder[AuthorDetail] = DeriveJsonEncoder.gen

  def isEmpty(author: AuthorDetail): Boolean =
    author.title.isEmpty &&
    author.alias.isEmpty &&
    author.description.isEmpty &&
    author.imageUrl.isEmpty
