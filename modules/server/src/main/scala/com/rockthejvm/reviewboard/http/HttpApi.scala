package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.*
import zio.*
import sttp.tapir.server.ServerEndpoint

object HttpApi {

  def gatherRoutes(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers = for
    healthController  <- HealthController.makeZIO
    companyController <- CompanyController.makeZIO
  yield List(healthController, companyController)

  val endpointsZIO = makeControllers.map(gatherRoutes)

}
