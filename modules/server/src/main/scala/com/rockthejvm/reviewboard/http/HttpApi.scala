package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.*
import sttp.tapir.server.ServerEndpoint
import zio.*

object HttpApi {

  def gatherRoutes(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers =
    for
      healthController  <- HealthController.makeZIO
      companyController <- CompanyController.makeZIO
      reviewController  <- ReviewController.makeZIO
      userController    <- UserController.makeZIO
      inviteController  <- InviteController.makeZIO
    yield List(healthController, companyController, reviewController, userController, inviteController)

  val endpointsZIO = makeControllers.map(gatherRoutes)

}
