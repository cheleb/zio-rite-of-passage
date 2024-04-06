package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

case class Choices(
    index: Int,
    message: Message,
    logprobs: Option[String],
    finish_reason: String
) derives JsonCodec

case class Message(
    role: String,
    content: String
) derives JsonCodec

case class CompletionResponse(
    id: String,
    `object`: String,
    created: Int,
    model: String,
    system_fingerprint: Option[String],
    choices: Seq[Choices],
    usage: Usage
) derives JsonCodec

case class Usage(
    prompt_tokens: Int,
    completion_tokens: Int,
    total_tokens: Int
) derives JsonCodec
