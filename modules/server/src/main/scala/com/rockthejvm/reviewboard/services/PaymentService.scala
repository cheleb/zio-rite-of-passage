package com.rockthejvm.reviewboard.services

import zio.*
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import com.rockthejvm.reviewboard.config.StripeConfig
import com.rockthejvm.reviewboard.config.Configs
import com.stripe.Stripe
import com.stripe.net.Webhook
import scala.jdk.OptionConverters.*
import com.stripe.model.Event
import com.rockthejvm.reviewboard.repositories.InviteRepository

trait PaymentService {
  def createCheckoutSession(invitePackId: Long, userName: String): Task[Option[Session]]
  def handleWebhookEvent(signature: String, payload: String, action: Long => Task[Boolean]): Task[Boolean]
}

class PaymentServiceLive private (config: StripeConfig) extends PaymentService {
  def createCheckoutSession(invitePackId: Long, userName: String): Task[Option[Session]] =
    ZIO.attempt(SessionCreateParams.builder()
      .setMode(SessionCreateParams.Mode.PAYMENT)
      .setSuccessUrl(config.successUrl)
      .setCancelUrl(config.cancelUrl)
      .setCustomerEmail(userName)
      .setClientReferenceId(invitePackId.toString)
      .setInvoiceCreation(SessionCreateParams.InvoiceCreation.Builder().setEnabled(true).build())
      .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
        .setDescription("Invite pack purchase")
        .setReceiptEmail(userName)
//        .setMetadata(Map("invitePackId" -> invitePackId.toString).asJava)
        .build())
      .addLineItem(
        SessionCreateParams.LineItem.builder()
          .setPrice(config.price)
          .setQuantity(1L)
          .build()
      )
      .build()).map(params => Session.create(params))
      .map(Option(_))
      .logError("Failed to create Stripe session")
      .catchSome { case _ => ZIO.none }

  def handleWebhookEvent(signature: String, payload: String, action: Long => Task[Boolean]): Task[Boolean] =
    ZIO.attempt(
      Webhook.constructEvent(payload, signature, config.webhookSecret)
    ).flatMap { event =>
      event.getType match {
        case "checkout.session.completed" =>
          for {
            packId    <- extractPackId(event)
            activated <- action(packId) // action(packId
          } yield activated

        case _ => ZIO.succeed(false)
      }
    }
      .logError("Failed to handle Stripe webhook event")
      .catchSome { case _ => ZIO.succeed(false) }

  private def extractPackId(event: Event): Task[Long] =
    ZIO.attempt {
      val deserializer = event.getDataObjectDeserializer
      val session      = deserializer.getObject().toScala.map(_.asInstanceOf[Session])
      session.flatMap(s => Option(s.getClientReferenceId)).map(_.toLong)

    }.someOrFail(new RuntimeException("Failed to extract invite pack ID from Stripe event"))

}

object PaymentServiceLive {
  val layer = ZLayer {
    for {
      config <- ZIO.service[StripeConfig]
      _ = Stripe.apiKey = config.apiKey
    } yield PaymentServiceLive(config)
  }

  val configuredLayer = Configs.makeConfigLayer[StripeConfig]("rockthejvm.stripe") >>> layer
}
