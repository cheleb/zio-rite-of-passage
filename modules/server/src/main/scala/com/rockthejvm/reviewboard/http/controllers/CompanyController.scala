package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import zio.*
import sttp.tapir.*
import scala.collection.mutable
import com.rockthejvm.reviewboard.domain.data.*
import sttp.tapir.server.ServerEndpoint
import com.rockthejvm.reviewboard.services.*

class CompanyController private (jwtService: JWTService, companyService: CompanyService)
    extends SecuredBaseController(jwtService)
    with CompanyEndpoints {
  // implement your company endpoint logic here

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .withSecurity
    .serverLogic(userId => req => companyService.create(req).either)

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

  val delete: ServerEndpoint[Any, Task] = deleteEndpoint
    .withSecurity
    .serverLogic { userId => id =>
      companyService.delete(id).either
    }

  val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, findById, delete)
}

object CompanyController {
  val makeZIO =
    for {
      jwtService     <- ZIO.service[JWTService]
      companyService <- ZIO.service[CompanyService]
    } yield new CompanyController(jwtService, companyService)

}
