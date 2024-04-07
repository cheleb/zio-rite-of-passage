package com.rockthejvm.reviewboard.config

import zio.Config
import zio.config.magnolia.deriveConfig

final case class StripeConfig(
    apiKey: String,
    webhookSecret: String,
    price: String,
    successUrl: String,
    cancelUrl: String
)

object StripeConfig:
  given Config[StripeConfig] = deriveConfig
