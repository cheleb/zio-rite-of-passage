package com.rockthejvm.reviewboard.services

import zio.*

import java.util.Properties
import javax.mail.*
import javax.mail.internet.MimeMessage

import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.EmailServiceConfig
import com.rockthejvm.reviewboard.domain.data.Company

trait EmailService {
  def sendEmail(to: String, subject: String, content: String): Task[Unit]
  def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit] = {
    val subject = "Password recovery"
    val content = s"""
    <div>
      <h1>Password recovery</h1>
      <p>
        You have requested a password recovery. If you did not request this, please ignore this email.
      </p>
      <p>
        If you did request a password recovery, please click on the following link to reset your password:
      </p>
      <a href="http://localhost:8080/reset-password?token=$token">Reset password</a>
    Your password recovery token is: $token"""
    sendEmail(to, subject, content)
  }

  def sendReviewInviteEmail(from: String, to: String, company: Company): Task[Unit] = {
    val subject = s"You've been invited to review ${company.name}"
    val content = s"""
    <div>
      <h1>You've been invited to review ${company.name} request</h1>
      <p>
        You've been invited to review a company on our platform. If you did not request this, please ignore this email.
      </p>
      <p>
        If you did request to review a pull request, please click on the following link to access the review:
      </p>
      <a href="http://localhost:8080/company/${company.id}">Review plz</a>
    """
    sendEmail(to, subject, content)
  }
}

class EmailServiceLive private (config: EmailServiceConfig) extends EmailService {

  private val host: String     = config.host
  private val port: Int        = config.port
  private val username: String = config.username
  private val password: String = config.password

  def sendEmail(to: String, subject: String, content: String): Task[Unit] =
    for
      props   <- propsResource
      session <- createSession(props)
      message <- createMessage(session)(username, to, subject, content)
    yield Transport.send(message)

  private val propsResource: Task[Properties] =
    ZIO.succeed {
      val prop = new Properties()
      prop.put("mail.smtp.auth", true)
      prop.put("mail.smtp.starttls.enable", "true")
      prop.put("mail.smtp.host", host)
      prop.put("mail.smtp.port", port)
      prop.put("mail.smtp.ssl.trust", host)
      prop
    }

  private def createSession(props: Properties): Task[Session] =
    ZIO.attempt {
      Session.getInstance(
        props,
        new Authenticator {
          override protected def getPasswordAuthentication: PasswordAuthentication =
            new PasswordAuthentication(username, password)
        }
      )
    }

  private def createMessage(
      session: Session
  )(from: String, to: String, subject: String, content: String): Task[MimeMessage] = {
    val message = new MimeMessage(session)
    message.setFrom(from)
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(content, "text/html; charset=utf-8")
    ZIO.succeed(message)
  }
}

object EmailServiceLive {
  val layer = ZLayer.fromFunction(EmailServiceLive(_))

  val configuredLayer =
    Configs.makeConfigLayer[EmailServiceConfig]("rockthejvm.email") >>> EmailServiceLive.layer
}
