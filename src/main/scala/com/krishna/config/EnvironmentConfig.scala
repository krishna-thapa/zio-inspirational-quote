package com.krishna.config

import zio.*
import zio.config.*
import zio.config.magnolia.{Descriptor, descriptor}
import zio.config.typesafe.TypesafeConfigSource

trait EnvironmentConfig:
  val configPath: String

  def getEnvironmentConfig[T: Tag](using Descriptor[T]): ZLayer[Any, ReadError[String], T] =
    ZLayer {
      read {
        descriptor[T].from(
          TypesafeConfigSource
            .fromResourcePath
            .at(PropertyTreePath.$(configPath))
        )
      }
    }
