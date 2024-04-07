package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.DeriveConfig

final case class RecoveryTokensConfig(duration: Long)
import zio.config.magnolia.deriveConfig
object RecoveryTokensConfig:
  given Config[RecoveryTokensConfig] = deriveConfig[RecoveryTokensConfig]
