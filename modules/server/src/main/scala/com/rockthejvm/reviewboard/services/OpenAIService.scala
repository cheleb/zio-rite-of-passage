package com.rockthejvm.reviewboard.services

import zio.*

trait OpenAIService {
  def getCompletion(prompt: String): Task[Option[String]]
}

class OpenAIServiceLive extends OpenAIService {
  override def getCompletion(prompt: String): Task[Option[String]] = ZIO.succeed(Some("This is a completion"))
}

object OpenAIServiceLive {
  val layer = ZLayer.succeed(new OpenAIServiceLive)
}
