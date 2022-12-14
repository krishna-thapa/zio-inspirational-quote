package com.krishna.wikiHttp

import zio.ZIO

import com.krishna.configuration.WikiConfig
import com.krishna.model.AuthorDetail
import com.krishna.wikiHttp.JsonRes.JsonBody

/** Service definition for the Web client that can be use for the DI */
trait WebClient:

  def getWebClientResponse(
    url: String,
    params: Seq[(String, String)] = Nil,
    headers: Seq[(String, String)] = Nil
  ): ZIO[Any, Throwable, JsonBody]

  def getAuthorDetail(
    author: String
  ): ZIO[WebClient with WikiConfig, Throwable, AuthorDetail]

object WebClient:

  // front-facing API, aka "accessor"
  def getWebClientResponse(
    url: String,
    params: Seq[(String, String)] = Nil,
    headers: Seq[(String, String)] = Nil
  ): ZIO[WebClient, Throwable, JsonBody] =
    ZIO.serviceWithZIO[WebClient](_.getWebClientResponse(url, params, headers))

  def getAuthorDetail(
    author: String
  ): ZIO[WebClient with WikiConfig, Throwable, AuthorDetail] =
    ZIO.serviceWithZIO[WebClient](_.getAuthorDetail(author))
