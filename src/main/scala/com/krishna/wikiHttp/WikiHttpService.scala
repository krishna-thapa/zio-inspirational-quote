package com.krishna.wikiHttp

import zio.*
import zio.http.Client

import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody
import com.krishna.wikiHttp.WikiHttpApi.*

case class WikiHttpService() extends WebClient:

  override def getWebClientResponse(
    url: String,
    params: Seq[(String, String)],
    headers: Seq[(String, String)]
  ): ZIO[Any, Throwable, JsonBody] =
    val program: ZIO[Client, Throwable, JsonBody] =
      for
        response <- Client.request(url)
        jsonBody <- response.body.asString
      yield JsonBody(jsonBody)

    program.provide(Client.default)

  override def getAuthorDetail(
    author: String
  ): ZIO[WebClient, Throwable, AuthorDetail] =
    getAuthorDetailFromUrl(author)

object WikiHttpService:
  val layer: ULayer[WikiHttpService] = ZLayer.succeed(WikiHttpService())
