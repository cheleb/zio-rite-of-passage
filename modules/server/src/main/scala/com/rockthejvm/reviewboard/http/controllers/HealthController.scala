package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint
import zio.*
import sttp.tapir.*

class HealthController private extends HealthEndpoint {
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
