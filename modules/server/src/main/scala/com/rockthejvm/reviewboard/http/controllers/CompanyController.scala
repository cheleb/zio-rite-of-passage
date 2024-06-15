package com.rockthejvm.reviewboard.http.controllers

import zio.*

import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.services.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

class CompanyController private (jwtService: JWTService, companyService: CompanyService)
    extends SecuredBaseController(jwtService) {
  // implement your company endpoint logic here

  val create: ServerEndpoint[Any, Task] = CompanyEndpoints.create
    .securedServerLogic(userId => req => companyService.create(req))

  val getAll: ServerEndpoint[Any, Task] =
    CompanyEndpoints.getAll.zServerLogic(_ => companyService.getAll)

  val findById: ServerEndpoint[Any, Task] = CompanyEndpoints.findById.zServerLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(companyService.getById)
      .catchSome { case _: NumberFormatException =>
        companyService.getBySlug(id)
      }

  }

  val delete: ServerEndpoint[Any, Task] = CompanyEndpoints.delete
    .securedServerLogic { userId => id =>
      companyService.delete(id)
    }

  val allFilters: ServerEndpoint[Any, Task] =
    CompanyEndpoints.allFilters.zServerLogic(_ => companyService.allFilters)

  val search: ServerEndpoint[Any, Task] =
    CompanyEndpoints.search.zServerLogic(filter => companyService.search(filter))
  val routes: List[ServerEndpoint[Any, Task]] =
    List(create, getAll, allFilters, search, findById, delete)
}

object CompanyController {
  val makeZIO =
    for {
      jwtService     <- ZIO.service[JWTService]
      companyService <- ZIO.service[CompanyService]
    } yield new CompanyController(jwtService, companyService)

}
