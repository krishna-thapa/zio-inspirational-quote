package com.krishna.wikiHttp

import java.net.URLEncoder

import zio.json.*
import zio.{ ZIO, ZLayer }

import com.krishna.model.AuthorDetail
import com.krishna.util.DbUtils
import com.krishna.wikiHttp.JsonRes.JsonBody

object WikiHttpApi:

  case class Terms(alias: Seq[String] = Seq.empty, description: Seq[String] = Seq.empty)

  given JsonDecoder[Terms] = DeriveJsonDecoder.gen[Terms]

  case class Thumbnail(source: Option[String] = None)

  given JsonDecoder[Thumbnail] = DeriveJsonDecoder.gen[Thumbnail]

  case class WikiDetails(terms: Terms, thumbnail: Thumbnail)

  given JsonDecoder[WikiDetails] = DeriveJsonDecoder.gen[WikiDetails]

  case class Pages(pages: Seq[WikiDetails])

  given JsonDecoder[Pages] = DeriveJsonDecoder.gen[Pages]

  case class Entity(query: Pages)

  given JsonDecoder[Entity] = DeriveJsonDecoder.gen[Entity]

  private val toAuthorDetail: (Array[String], String) => AuthorDetail =
    (authorWithInfo: Array[String], jsonData: String) =>
      val title: String = authorWithInfo.head.trim
      jsonData.fromJson[Entity].toOption match
        case Some(result) =>
          AuthorDetail(
            title,
            alias = result.query.pages.flatMap(_.terms.alias),
            description = result.query.pages.flatMap(_.terms.description),
            imageUrl = result.query.pages.map(_.thumbnail.source).head
          )
        case None         => AuthorDetail(title)

  private val capitalizeAuthor = (author: String) =>
    author.split(" ").map(_.trim.capitalize).mkString(" ")

  private val encodeAuthor = (author: String) => URLEncoder.encode(author, "UTF-8")

  private def filterAuthor(author: String): String =
    // TODO: Remove the initial from the authors
    Seq(author)
      .map(capitalizeAuthor)
      .map(encodeAuthor)
      .head

  def getAuthorDetailFromUrl(
    author: String
  ): ZIO[WebClient, Throwable, AuthorDetail] =
    val splitAuthorWithInfo: Array[String] = author.split(",")
    val encodedAuthor: String              = filterAuthor(splitAuthorWithInfo.head)
    for
      wikiConfig  <- DbUtils.getWikiConfig
      jsonContent <- WebClient.getWebClientResponse(
        wikiConfig.apiUrl.concat(encodedAuthor)
      )
      authorDetail = toAuthorDetail(splitAuthorWithInfo, jsonContent.value)
    yield authorDetail
