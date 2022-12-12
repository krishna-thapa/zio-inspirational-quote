package com.krishna.main

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.{ ZIO, ZIOAppDefault, ZLayer }
import java.sql.SQLException
import com.krishna.model.InspirationalQuote

class DataService(quill: Quill.Postgres[SnakeCase]):
  import quill._

  val circles = quote {
    querySchema[InspirationalQuote]("serial_id", _.quote -> "quote", _.author -> "author", "genre")
  }
  def getPeople: ZIO[Any, SQLException, List[InspirationalQuote]] = run(query[InspirationalQuote])

object DataService:
  def getPeople: ZIO[DataService, SQLException, List[InspirationalQuote]] =
    ZIO.serviceWithZIO[DataService](_.getPeople)

  val live = ZLayer.fromFunction(new DataService(_))
