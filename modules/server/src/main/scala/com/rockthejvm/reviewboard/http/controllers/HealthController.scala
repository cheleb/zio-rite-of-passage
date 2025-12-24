package com.rockthejvm.reviewboard.http.controllers

import zio.*

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint

import sttp.tapir.server.ServerEndpoint

class HealthController private extends BaseController {

  val health = HealthEndpoint.health
    .serverLogicSuccess[Task](_ => ZIO.succeed("OK"))

  val error = HealthEndpoint.error
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("boom!")).either)
  override val routes: List[ServerEndpoint[Any, Task]] = List(health, error)
}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
