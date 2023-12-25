package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import zio.*
import sttp.tapir.*
import scala.collection.mutable
import com.rockthejvm.reviewboard.domain.data.Company
import sttp.tapir.server.ServerEndpoint
class CompanyController private extends BaseController with CompanyEndpoints {
  // implement your company endpoint logic here
  private val db = mutable.Map.empty[Long, Company]

  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverLogicSuccess { req =>
      val id            = db.keys.max + 1
      val companyWithId = req.toCompany(id)
      db += (id -> companyWithId)
      ZIO.succeed(companyWithId)
    }

  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogicSuccess { _ =>
    ZIO.succeed(db.values.toList)
  }

  val findById: ServerEndpoint[Any, Task] = findByIdEndpoint.serverLogicSuccess { id =>
    ZIO.succeed(db.values.find(_.slug == id))
  }

  val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, findById)
}

object CompanyController {
  val makeZIO = ZIO.succeed(new CompanyController)
}
