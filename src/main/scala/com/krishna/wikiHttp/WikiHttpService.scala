package com.krishna.wikiHttp

import zio.http.service.{ ChannelFactory, EventLoopGroup }
import zio.{ ULayer, ZIO, ZLayer }
import com.krishna.config.WikiConfig
import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody
import com.krishna.wikiHttp.WikiHttpApi.*

case class WikiHttpService() extends WebClient:

  override def getWebClientResponse(
    url: String,
    params: Seq[(String, String)],
    headers: Seq[(String, String)]
  ): ZIO[Any, Throwable, JsonBody] =
    val program
      : ZIO[zio.http.service.EventLoopGroup & zio.http.service.ChannelFactory, Throwable, JsonBody] =
      for
        response <- Client.request(url)
        jsonBody <- response.body.asString
      yield JsonBody(jsonBody)

    program.provide(ChannelFactory.auto ++ EventLoopGroup.auto())

  override def getAuthorDetail(
    author: String
  ): ZIO[WebClient with WikiConfig, Throwable, AuthorDetail] =
    getAuthorDetailFromUrl(author)

object WikiHttpService:
  val layer: ULayer[WikiHttpService] = ZLayer.succeed(WikiHttpService())
