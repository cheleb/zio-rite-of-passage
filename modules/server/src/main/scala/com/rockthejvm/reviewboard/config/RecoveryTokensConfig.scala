package com.rockthejvm.reviewboard.config

import zio.config.magnolia.DeriveConfig
import zio.Config

final case class RecoveryTokensConfig(duration: Long)
import zio.config.magnolia.deriveConfig
object RecoveryTokensConfig:
  given Config[RecoveryTokensConfig] = deriveConfig[RecoveryTokensConfig]
