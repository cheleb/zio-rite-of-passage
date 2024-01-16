package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import zio.*
import sttp.tapir.*
import scala.collection.mutable
import com.rockthejvm.reviewboard.domain.data.Company
import sttp.tapir.server.ServerEndpoint
import com.rockthejvm.reviewboard.services.CompanyService

class CompanyController private (companyService: CompanyService)
    extends BaseController
    with CompanyEndpoints {
  // implement your company endpoint logic here

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverLogic(companyService.create(_).either)

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.serverLogic(_ => companyService.getAll.either)

  val findById: ServerEndpoint[Any, Task] = findByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(companyService.getById)
      .catchSome { case _: NumberFormatException =>
        companyService.getBySlug(id)
      }
      .either
  }

  val delete: ServerEndpoint[Any, Task] = deleteEndpoint.serverLogic { id =>
    companyService.delete(id).either
  }

  val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, findById, delete)
}

object CompanyController {
  val makeZIO =
    for {
      companyService <- ZIO.service[CompanyService]
    } yield new CompanyController(companyService)

}
