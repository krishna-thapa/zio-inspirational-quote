package com.krishna.wikiHttp

import com.krishna.config.EnvironmentConfig
import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody
import com.krishna.wikiHttp.WikiHttpApi.*
import zhttp.service.{ ChannelFactory, Client, EventLoopGroup }
import zio.{ ULayer, ZIO, ZLayer }

case class WikiHttpService() extends WebClient:

  override def getWebClientResponse(
    url: String,
    params: Seq[(String, String)],
    headers: Seq[(String, String)]): ZIO[Any, Throwable, JsonBody] =
    val program
      : ZIO[zhttp.service.EventLoopGroup & zhttp.service.ChannelFactory, Throwable, JsonBody] =
      for
        response <- Client.request(url)
        jsonBody <- response.body.asString
      yield JsonBody(jsonBody)

    program.provide(ChannelFactory.auto ++ EventLoopGroup.auto())

  override def getAuthorDetail(
    author: String): ZIO[WebClient with EnvironmentConfig, Throwable, AuthorDetail] =
    getAuthorDetailFromUrl(author)

object WikiHttpService:
  val layer: ULayer[WikiHttpService] = ZLayer.succeed(WikiHttpService())
