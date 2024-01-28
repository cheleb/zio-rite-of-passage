package com.rockthejvm.reviewboard.config

import scala.concurrent.duration.Duration

final case class JWTConfig(secret: String, issuer: String, ttl: Duration)
