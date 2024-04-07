package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.deriveConfig

final case class SummaryConfig(minReviews: Int, nSelected: Int)

object SummaryConfig:
  given Config[SummaryConfig] = deriveConfig
