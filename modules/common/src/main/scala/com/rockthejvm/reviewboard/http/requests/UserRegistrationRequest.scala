package com.rockthejvm.reviewboard.http.requests
import zio.json.JsonCodec

final case class UserRegistrationRequest(
    email: String,
    password: String
) derives JsonCodec
