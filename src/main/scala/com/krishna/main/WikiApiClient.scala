package com.krishna.main

import com.krishna.wikiHttp.{ WebClient, WikiHttpApi, WikiHttpService }
import zio.*

import java.net.URLEncoder

object WikiApiClient extends ZIOAppDefault {

  val program = for {
    res <- WebClient.getAuthorDetail("Friedrich Nietzsche")
    _ <- Console.printLine(res)
  } yield ()

  override val run = program.provide(WikiHttpService.layer)

}
