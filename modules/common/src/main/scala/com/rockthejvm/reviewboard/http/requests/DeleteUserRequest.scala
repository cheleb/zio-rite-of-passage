package com.rockthejvm.reviewboard.http.requests
import zio.json.JsonCodec

final case class DeleteUserRequest(
    email: String,
    password: String
) derives JsonCodec
