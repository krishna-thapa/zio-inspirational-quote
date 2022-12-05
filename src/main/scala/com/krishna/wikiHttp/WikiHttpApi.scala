package com.krishna.wikiHttp

import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody
import zio.json.*
import zio.{ ZIO, ZLayer }

import java.net.URLEncoder

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

  private val toAuthorDetail: (String, String) => AuthorDetail =
    (author: String, jsonData: String) =>
      jsonData.fromJson[Entity].toOption match
        case Some(result) =>
          AuthorDetail(
            title = author,
            alias = result.query.pages.flatMap(_.terms.alias),
            description = result.query.pages.flatMap(_.terms.description),
            imagerUrl = result.query.pages.map(_.thumbnail.source).head,
          )
        case None => AuthorDetail(title = author)

  private val capitalizeAuthor = (author: String) =>
    author.split(" ").map(_.trim.capitalize).mkString(" ")

  private val encodeAuthor = (author: String) => URLEncoder.encode(author, "UTF-8")

  private def filterAuthor(author: String): String =
    // TODO: Remove the initial from the authors
    Seq(author)
      .map(capitalizeAuthor)
      .map(encodeAuthor)
      .head

  def getAuthorDetailFromUrl(author: String): ZIO[WebClient, Throwable, AuthorDetail] =
    // TODO: Get it from the config file
    val encodedAuthor: String = filterAuthor(author)
    val url: String =
      s"https://en.wikipedia.org/w/api.php?action=query&format=json&formatversion=2&prop=pageimages%7Cpageterms&piprop=thumbnail&pithumbsize=500&titles=$encodedAuthor"
    for
      jsonContent <- WebClient.getWebClientResponse(url)
      authorDetail = toAuthorDetail(author, jsonContent.value)
    yield authorDetail
