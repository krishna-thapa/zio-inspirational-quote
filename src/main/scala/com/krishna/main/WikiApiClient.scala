package com.krishna.main

import zhttp.http.Headers
import zhttp.service.{ ChannelFactory, Client, EventLoopGroup }
import zio.*
import zio.json.*
import zio.json.ast.{ Json, JsonCursor }

import java.net.URLEncoder

object WikiApiClient extends ZIOAppDefault {
  case class Terms(alias: Seq[String], description: Seq[String])
  implicit val decoderTerms: JsonDecoder[Terms] = DeriveJsonDecoder.gen[Terms]

  case class Thumbnail(source: String)
  implicit val decoderThumbnail: JsonDecoder[Thumbnail] = DeriveJsonDecoder.gen[Thumbnail]

  case class WikiDetails(terms: Terms, thumbnail: Thumbnail)
  implicit val decoderWikiDetails: JsonDecoder[WikiDetails] = DeriveJsonDecoder.gen[WikiDetails]

  case class Pages(pages: Seq[WikiDetails])
  implicit val decoderPages: JsonDecoder[Pages] = DeriveJsonDecoder.gen[Pages]

  case class Entity(query: Pages)
  implicit val decoder: JsonDecoder[Entity] = DeriveJsonDecoder.gen[Entity]

  val author = "Suzanne Collins"
  val params: String = URLEncoder.encode(author, "UTF-8")

  val program = for {
    res <- Client.request(
      s"https://en.wikipedia.org/w/api.php?action=query&format=json&formatversion=2&prop=pageimages%7Cpageterms&piprop=thumbnail&pithumbsize=500&titles=$params"
    )
    data <- res.body.asString
    foo = data.fromJson[Entity]
    _ <- Console.printLine(foo)
  } yield ()

  override val run = program.provide(ChannelFactory.auto ++ EventLoopGroup.auto())

}
