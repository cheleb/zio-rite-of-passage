package com.rockthejvm.reviewboard.config

import zio.Duration

final case class JWTConfig(secret: String, issuer: String, ttl: Duration)
