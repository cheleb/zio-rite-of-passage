package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {

  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))

  val error = errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("boom!")).either)
  override val routes: List[ServerEndpoint[Any, Task]] = List(health, error)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
