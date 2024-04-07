package com.rockthejvm.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider
object Configs:
  def makeConfigLayer[C](path: String)(using conf: Config[C], r: Tag[C]) =
    ZLayer(TypesafeConfigProvider.fromTypesafeConfig(
      ConfigFactory.load().getConfig(path)
    ).load[C](conf))
