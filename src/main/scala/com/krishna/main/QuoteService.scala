// package com.krishna.main

// import io.getquill._
// import io.getquill.jdbczio.Quill
// import zio.{ ZIO, ZIOAppDefault, ZLayer }
// import java.sql.SQLException
// import com.krishna.model.InspirationalQuote

// trait QuoteService:
//   def insertQuote(quoteRecord: InspirationalQuote): ZIO[Any, SQLException, Long]

// class QuoteDataService(quill: Quill.Postgres[SnakeCase]) extends QuoteService:
//   import quill.*

//   def insertQuote(quoteRecord: InspirationalQuote): ZIO[Any, SQLException, Long] =

//     val insertQuery = quote {
//       querySchema[InspirationalQuote]("quotes")
//         .insert(
//           _.serialId -> lift(quoteRecord.serialId),
//           _.quote -> lift(quoteRecord.quote),
//          // _.genre -> lift(quoteRecord.genre),
//           _.storedDate -> lift(quoteRecord.storedDate)
//         )
//     }
//     run(insertQuery)

// object QuoteService:
//   def QuoteService(quoteRecord: InspirationalQuote): ZIO[QuoteDataService, SQLException, Long] =
//     ZIO.serviceWithZIO[QuoteService](_.insertQuote(quoteRecord))

//   val live = ZLayer.fromFunction(new QuoteDataService(_))
