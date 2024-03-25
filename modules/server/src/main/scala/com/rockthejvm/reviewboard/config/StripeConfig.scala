package com.rockthejvm.reviewboard.config

import zio.config.magnolia.deriveConfig
import zio.Config

final case class StripeConfig(
    apiKey: String,
    webhookSecret: String,
    price: String,
    successUrl: String,
    cancelUrl: String
)

object StripeConfig:
  given Config[StripeConfig] = deriveConfig
