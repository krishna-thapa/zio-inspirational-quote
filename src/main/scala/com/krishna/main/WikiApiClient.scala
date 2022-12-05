package com.krishna.main

import com.krishna.wikiHttp.{ WebClient, WikiHttpApi, WikiHttpService }
import zio.*

import java.net.URLEncoder

object WikiApiClient extends ZIOAppDefault:

  val program: ZIO[WebClient, Throwable, Unit] =
    for
      res <- WebClient.getAuthorDetail("")
      _ <- Console.printLine(res)
    yield ()

  override val run: ZIO[Any, Throwable, Unit] = program.provide(WikiHttpService.layer)
