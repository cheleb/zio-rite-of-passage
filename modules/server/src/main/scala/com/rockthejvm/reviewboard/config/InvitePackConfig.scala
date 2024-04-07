package com.rockthejvm.reviewboard.config

import zio.Config
import zio.Duration
import zio.config.magnolia.deriveConfig

final case class InvitePackConfig(nInvites: Int, invitePackPrice: Double, invitePackValidity: Duration)

object InvitePackConfig:
  given Config[InvitePackConfig] = deriveConfig
