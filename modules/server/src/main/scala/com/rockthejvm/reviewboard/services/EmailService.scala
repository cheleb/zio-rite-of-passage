package com.rockthejvm.reviewboard.services

import zio.*

trait EmailService {
  def sendEmail(email: String, subject: String, content: String): Task[Unit]
  def sendPasswordRecoveryEmail(email: String, token: String): Task[Unit]
}

class EmailServiceLive private extends EmailService {
  override def sendEmail(email: String, subject: String, content: String): Task[Unit] =
    ZIO.fail(new RuntimeException("Not implemented"))
  override def sendPasswordRecoveryEmail(email: String, token: String): Task[Unit] =
    ZIO.fail(new RuntimeException("Not implemented"))
}

object EmailServiceLive {
  def layer = ZLayer.succeed(new EmailServiceLive)
}
