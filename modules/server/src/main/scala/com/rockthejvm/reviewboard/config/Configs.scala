package com.rockthejvm.reviewboard.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfig
import com.typesafe.config.ConfigFactory

object Configs:
  def makeConfigLayer[C](path: String)(using Descriptor[C], Tag[C]) =
    TypesafeConfig.fromTypesafeConfig(
      ZIO.attempt(ConfigFactory.load().getConfig(path)),
      descriptor[C]
    )
