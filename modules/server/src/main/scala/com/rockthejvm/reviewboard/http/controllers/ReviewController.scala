package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint

import zio.Task
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoint
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.domain.data.Review
import zio.ZIO

class ReviewController private (reviewService: ReviewService)
    extends BaseController
    with ReviewEndpoint {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint
      .serverLogic { request =>
        reviewService.create(request, -1L).either // FIXME get the user id from the request
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
  val makeZIO = ZIO.service[ReviewService].map(ReviewController(_))
}
