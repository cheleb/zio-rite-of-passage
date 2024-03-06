package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint

import zio.*
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoint
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.server.PartialServerEndpoint

class ReviewController private (jwtService: JWTService, reviewService: ReviewService)
    extends SecuredBaseController(jwtService)
    with ReviewEndpoint {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .withSecurity
      .zioServerLogic { userId => request =>
        reviewService.create(request, userId.id)
      }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint
      .zioServerLogic { id =>
        reviewService.getById(id)
      }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint
      .zioServerLogic { companyId =>
        reviewService.getByCompanyId(companyId)
      }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId)

}

object ReviewController {
  val makeZIO =
    for {
      jwtService    <- ZIO.service[JWTService]
      reviewService <- ZIO.service[ReviewService]
    } yield new ReviewController(jwtService, reviewService)
}
