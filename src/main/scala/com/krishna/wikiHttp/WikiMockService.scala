package com.krishna.wikiHttp

import zio.{ ULayer, ZIO, ZLayer }

import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody

case class WikiMockService() extends WebClient:

  override def getWebClientResponse(
    url: String,
    params: Seq[(String, String)],
    headers: Seq[(String, String)]
  ): ZIO[Any, Throwable, JsonBody] =
    ZIO.succeed(JsonBody("mock JSON Body"))

  override def getAuthorDetail(author: String): ZIO[WebClient, Throwable, AuthorDetail] =
    getWebClientResponse("mock url", Nil, Nil) *>
      ZIO.logInfo(s"Mocking Wiki API call for the author: $author") *>
      ZIO.succeed(AuthorDetail(title = "mock Author"))

object WikiMockService:
  val layer: ULayer[WikiMockService] = ZLayer.succeed(WikiMockService())
