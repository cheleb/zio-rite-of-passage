package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

final case class UserResponse(email: String) derives JsonCodec
