package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.deriveConfig

final case class EmailServiceConfig(host: String, port: Int, username: String, password: String)

object EmailServiceConfig:
  given Config[EmailServiceConfig] = deriveConfig[EmailServiceConfig]
