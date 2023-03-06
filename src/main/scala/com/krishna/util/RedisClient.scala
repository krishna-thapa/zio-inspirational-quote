package com.krishna.util

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.profunktor.redis4cats.effect.Log.Stdout.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import zio.{ Task, ZIO }

import com.krishna.config.RedisConfig

trait RedisClient:

  val redisConfig: Task[RedisConfig]

  private val contentDate: String = DateConversion.getCurrentDate

  def getCachedQuote: ZIO[Any, Throwable, Option[String]] =
    val getFromRedis: RedisCommands[IO, String, String] => IO[Option[String]] =
      _.get(contentDate)
    redisConfig.flatMap { redisConfig =>
      redisRun(redisConfig.hostname, redisConfig.database, getFromRedis)
    }

  def setCachedQuote(quote: String): ZIO[Any, Throwable, Unit] =
    val setToRedis: RedisCommands[IO, String, String] => IO[Unit] =
      _.setEx(contentDate, quote, 1.minutes)
    redisConfig.flatMap { redisConfig =>
      redisRun(redisConfig.hostname, redisConfig.database, setToRedis)
    }

  private def redisRun[T](
    hostname: String,
    database: Int,
    redisFoo: RedisCommands[IO, String, String] => IO[T]
  ): ZIO[Any, Exception, T] =
    val toFutureVal: () => Future[T] = () =>
      Redis[IO]
        .utf8(s"redis://$hostname/$database")
        .use { redis =>
          for x <- redisFoo(redis)
          yield x
        }
        .unsafeToFuture()

    ZIO
      .fromFuture(_ => toFutureVal())
      .mapError(msg => new Exception(s"Error while running redis $msg"))
