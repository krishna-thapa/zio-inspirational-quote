package com.krishna.util

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.krishna.config.RedisConfig
import dev.profunktor.redis4cats.effect.Log.NoOp.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import zio.{ Task, ZIO }

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

trait RedisClient:

  val redisConfig: Task[RedisConfig]

  // private val cachedQuoteIdsKey: String = "cached_quote_ids"

  private val contentDate: String = DateConversion.getCurrentDate

  def getCachedQuote: ZIO[Any, Throwable, Option[String]] =
    val getFromRedis: RedisCommands[IO, String, String] => IO[Option[String]] =
      _.get(contentDate)
    redisConfig.flatMap { redisConfig =>
      redisRun(redisConfig, getFromRedis)
    }

  def setCachedQuote(quote: String): ZIO[Any, Throwable, Unit] =
    val setToRedis: RedisCommands[IO, String, String] => IO[Unit] =
      _.setEx(contentDate, quote, 1.minutes)
    redisConfig.flatMap { redisConfig =>
      redisRun(redisConfig, setToRedis)
    }

  def isPresentInCachedQuoteIds(quoteId: UUID): ZIO[Any, Throwable, Boolean] =
    // 1. Check if the quote Id is already present in the list of the cached quote Ids
    // 2. If it is present, return true or else return false

    val allQuoteIds: String => RedisCommands[IO, String, String] => IO[List[String]] =
      // -1 as stop integer allows you to get the whole range
      (redisKey: String) => _.lRange(redisKey, 0, -1)

    for
      config <- redisConfig
      ids    <- redisRun(config, allQuoteIds(config.cachedQuoteIdsKey))
    yield ids.contains(quoteId.toString)

  def setCachedQuoteIds(quoteId: UUID): ZIO[Any, Throwable, Unit] =
    // 1. Read the size of the List and compare to the max count
    // 2. If the list if equals to max count, then pop the last one and push the new one in the stack
    // 3. If the list is smaller than max count, then push the new one in the stack

    val findTotalLen: String => RedisCommands[IO, String, ?] => IO[Option[Long]] =
      (redisKey: String) => _.lLen(redisKey)

    val pushQuoteId: String => RedisCommands[IO, String, String] => IO[Long] =
      (redisKey: String) => _.lPush(redisKey, quoteId.toString)

    val popQuoteId: String => RedisCommands[IO, String, String] => IO[Option[String]] =
      (redisKey: String) => _.rPop(redisKey)

    for
      config <- redisConfig
      length <- redisRun(config, findTotalLen(config.cachedQuoteIdsKey))
      _      <-
        if length.isDefined && length.get == config.maxCacheQuoteIds then
          redisRun(config, popQuoteId(config.cachedQuoteIdsKey)).flatMap(_ =>
            redisRun(config, pushQuoteId(config.cachedQuoteIdsKey))
          )
        else redisRun(config, pushQuoteId(config.cachedQuoteIdsKey))
    yield ()

  private def redisRun[T](
    config: RedisConfig,
    redisFoo: RedisCommands[IO, String, String] => IO[T]
  ): ZIO[Any, Exception, T] =
    val toFutureVal: () => Future[T] = () =>
      Redis[IO]
        .utf8(s"redis://${config.hostname}/${config.database}")
        .use { redis =>
          for x <- redisFoo(redis)
          yield x
        }
        .unsafeToFuture()

    ZIO
      .fromFuture(_ => toFutureVal())
      .mapError(msg => new Exception(s"Error while running redis $msg"))
