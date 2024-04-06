package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

import zio.*
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.services.JWTService

class ReviewController private (jwtService: JWTService, reviewService: ReviewService)
    extends SecuredBaseController(jwtService)
    with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .securedServerLogic { userId => request =>
        reviewService.create(request, userId.id)
      }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint
      .zServerLogic { id =>
        reviewService.getById(id)
      }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint
      .zServerLogic { companyId =>
        reviewService.getByCompanyId(companyId)
      }

  val getSummary: ServerEndpoint[Any, Task] =
    getSummaryEndpoint
      .zServerLogic { companyId =>
        reviewService.getSummary(companyId)
      }

  val makeSummary: ServerEndpoint[Any, Task] =
    makeSummaryEndpoint
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
