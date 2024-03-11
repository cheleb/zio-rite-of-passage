package com.rockthejvm.reviewboard.domain.data

import zio.json.JsonCodec

final case class UserToken(id: Long, email: String, token: String, expires: Long) derives JsonCodec
