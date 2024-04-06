package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import zio.*
import sttp.tapir.ztapir.*
import com.rockthejvm.reviewboard.domain.data.*
import sttp.tapir.server.ServerEndpoint
import com.rockthejvm.reviewboard.services.*

class CompanyController private (jwtService: JWTService, companyService: CompanyService)
    extends SecuredBaseController(jwtService)
    with CompanyEndpoints {
  // implement your company endpoint logic here

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .securedServerLogic(userId => req => companyService.create(req))

  val getAll: ServerEndpoint[Any, Task] =
    getAllEndpoint.zServerLogic(_ => companyService.getAll)

  val findById: ServerEndpoint[Any, Task] = findByIdEndpoint.zServerLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(companyService.getById)
      .catchSome { case _: NumberFormatException =>
        companyService.getBySlug(id)
      }

  }

  val delete: ServerEndpoint[Any, Task] = deleteEndpoint
    .securedServerLogic { userId => id =>
      companyService.delete(id)
    }

  val allFilters: ServerEndpoint[Any, Task] =
    allFiltersEndpoint.zServerLogic(_ => companyService.allFilters)

  val search: ServerEndpoint[Any, Task] =
    searchEndpoint.zServerLogic(filter => companyService.search(filter))
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
