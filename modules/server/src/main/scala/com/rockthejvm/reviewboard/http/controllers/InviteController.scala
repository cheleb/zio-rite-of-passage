package com.rockthejvm.reviewboard.http.controllers

import zio.*

import com.rockthejvm.reviewboard
import com.rockthejvm.reviewboard.http.responses.*
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import reviewboard.http.endpoints.InviteEndpoints
import reviewboard.services.JWTService
import com.rockthejvm.reviewboard.domain.data.UserID

class InviteController private (jwtService: JWTService, inviteService: InviteService, paymentService: PaymentService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val addPack: ServerEndpoint[Any, Task] =
    InviteEndpoints.addPack.securedServerLogic(userId =>
      req =>
        inviteService.addInvitePack(userId.email, req.companyId).map(_.toString())
    )

  val invite: ServerEndpoint[Any, Task] =
    InviteEndpoints.invite.securedServerLogic(userId =>
      req =>
        inviteService.sendInvite(userId.email, req.companyId, req.emails)
          .map {
            case 0                           => InviteResponse("Failed to send invites", 0)
            case n if n == req.emails.length => InviteResponse("Invites sent successfully", n)
            case n                           => InviteResponse(s"Failed to send ${req.emails.length - n} invites", n)
          }
    )

  val getByUserId: ServerEndpoint[Any, Task] =
    InviteEndpoints.getByUserId.securedServerLogic(userId => _ => inviteService.getByUserName(userId.email))

  val addPackPromoted: ServerEndpoint[Any, Task] =
    InviteEndpoints.addPackPromoted
      .securedServerLogic {
        userId => req =>
          for {
            packId <- inviteService.addInvitePack(userId.email, req.companyId)
            session <- paymentService.createCheckoutSession(packId, userId.email)
              .someOrFail(new RuntimeException("Failed to create checkout session"))
          } yield session.getUrl()
      }

  val webhook: ServerEndpoint[Any, Task] =
    InviteEndpoints.webhook.zServerLogic { (signature, payload) =>
      paymentService.handleWebhookEvent(signature, payload, inviteService.activatePack).unit

    }
  override val routes: List[ServerEndpoint[Any, Task]] = List(addPack, addPackPromoted, webhook, invite, getByUserId)
}

object InviteController {
  val makeZIO =
    for {
      jwtService     <- ZIO.service[JWTService]
      inviteService  <- ZIO.service[InviteService]
      paymentService <- ZIO.service[PaymentService]
    } yield InviteController(jwtService, inviteService, paymentService)
}
