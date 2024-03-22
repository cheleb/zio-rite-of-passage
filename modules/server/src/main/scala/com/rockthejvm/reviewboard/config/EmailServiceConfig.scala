package com.rockthejvm.reviewboard.config

import zio.config.magnolia.deriveConfig
import zio.Config

final case class EmailServiceConfig(host: String, port: Int, username: String, password: String)

object EmailServiceConfig:
  given Config[EmailServiceConfig] = deriveConfig[EmailServiceConfig]
