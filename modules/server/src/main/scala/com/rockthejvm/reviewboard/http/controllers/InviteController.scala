package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard
import reviewboard.http.endpoints.InviteEndpoints
import reviewboard.services.JWTService
import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.ztapir.*

import com.rockthejvm.reviewboard.services.*
import com.rockthejvm.reviewboard.http.responses.*

class InviteController private (jwtService: JWTService, inviteService: InviteService, paymentService: PaymentService)
    extends SecuredBaseController(jwtService)
    with InviteEndpoints {

  val addPack: ServerEndpoint[Any, Task] =
    addPackEndpoint.securedServerLogic(userId =>
      req =>
        inviteService.addInvitePack(userId.email, req.companyId).map(_.toString())
    )

  val invite: ServerEndpoint[Any, Task] =
    inviteEndpoint.securedServerLogic(userId =>
      req =>
        inviteService.sendInvite(userId.email, req.companyId, req.emails)
          .map {
            case 0                           => InviteResponse("Failed to send invites", 0)
            case n if n == req.emails.length => InviteResponse("Invites sent successfully", n)
            case n                           => InviteResponse(s"Failed to send ${req.emails.length - n} invites", n)
          }
    )

  val getByUserId: ServerEndpoint[Any, Task] =
    getByUserIdEndpoint.securedServerLogic(userId => _ => inviteService.getByUserName(userId.email))

  val addPackPromoted: ServerEndpoint[Any, Task] =
    addPackPromotedEndpoint
      .securedServerLogic {
        userId => req =>
          for {
            packId <- inviteService.addInvitePack(userId.email, req.companyId)
            session <- paymentService.createCheckoutSession(packId, userId.email)
              .someOrFail(new RuntimeException("Failed to create checkout session"))
          } yield session.getUrl()
      }

  val webhook: ServerEndpoint[Any, Task] =
    webhookEndpoint.zServerLogic { (signature, payload) =>
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
