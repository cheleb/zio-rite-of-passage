package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.deriveConfig

final case class HttpConfig(port: Int)

object HttpConfig:
  given Config[HttpConfig] = deriveConfig[HttpConfig]
