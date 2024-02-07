package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint

import zio.*
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoint
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.services.JWTService

class ReviewController private (jwtService: JWTService, reviewService: ReviewService)
    extends SecuredBaseController(jwtService)
    with ReviewEndpoint {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .withSecurity
      .serverLogic { userId => request =>
        reviewService.create(request, userId.id).either // FIXME get the user id from the request
      }

  val getById: ServerEndpoint[Any, Task] =
    getByIdEndpoint
      .serverLogic { id =>
        reviewService.getById(id).either
      }

  val getByCompanyId: ServerEndpoint[Any, Task] =
    getByCompanyIdEndpoint
      .serverLogic { companyId =>
        reviewService.getByCompanyId(companyId).either
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
