package com.krishna.wikiHttp

import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody
import zio.{ ULayer, ZIO, ZLayer }

case class WikiMockService() extends WebClient:
  override def getWebClientResponse(
    url: String,
    params: Seq[(String, String)],
    headers: Seq[(String, String)]): ZIO[Any, Throwable, JsonBody] =

    ZIO.logInfo("foo") *> ZIO.succeed(JsonBody("ggg"))

  override def getAuthorDetail(author: String): ZIO[WebClient, Throwable, AuthorDetail] =
    getWebClientResponse("url", Nil, Nil) *> ZIO.logInfo("bar") *> ZIO.succeed(AuthorDetail(title = ""))

object WikiMockService:
  val layer: ULayer[WikiMockService] = ZLayer.succeed(WikiMockService())
