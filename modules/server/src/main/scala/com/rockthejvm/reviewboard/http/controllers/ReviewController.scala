package com.rockthejvm.reviewboard.http.controllers

import zio.*

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import com.rockthejvm.reviewboard.domain.data.UserID

class ReviewController private (jwtService: JWTService, reviewService: ReviewService)
    extends SecuredBaseController[String, UserID](jwtService.verifyToken) {

  val create: ServerEndpoint[Any, Task] =
    ReviewEndpoints.create
      .securedServerLogic { userId => request =>
        reviewService.create(request, userId.id)
      }

  val getById: ServerEndpoint[Any, Task] =
    ReviewEndpoints.getById
      .zServerLogic { id =>
        reviewService.getById(id)
      }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    ReviewEndpoints.getByCompanyId
      .zServerLogic { companyId =>
        reviewService.getByCompanyId(companyId)
      }

  val getSummary: ServerEndpoint[Any, Task] =
    ReviewEndpoints.getSummary
      .zServerLogic { companyId =>
        reviewService.getSummary(companyId)
      }

  val makeSummary: ServerEndpoint[Any, Task] =
    ReviewEndpoints.makeSummary
      .securedServerLogic { userId => companyId =>
        reviewService.makeSummary(companyId, userId.id)
      }
  override val routes: List[ServerEndpoint[Any, Task]] = List(getSummary, makeSummary, create, getById, getByCompanyId)

}

object ReviewController {
  val makeZIO =
    for {
      jwtService    <- ZIO.service[JWTService]
      reviewService <- ZIO.service[ReviewService]
    } yield new ReviewController(jwtService, reviewService)
}
