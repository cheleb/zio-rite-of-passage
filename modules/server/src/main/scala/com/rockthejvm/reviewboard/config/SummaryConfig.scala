package com.rockthejvm.reviewboard.config

import zio.config.magnolia.deriveConfig
import zio.Config

final case class SummaryConfig(minReviews: Int, nSelected: Int, promptTemplate: String)

object SummaryConfig:
  given Config[SummaryConfig] = deriveConfig
