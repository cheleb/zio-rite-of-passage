package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*

object HealthEndpoint extends BaseEndpoint {
  val health = baseEndpoint
    .tag("health")
    .name("health")
    .get
    .in("health")
    .out(plainBody[String])
    .description("Health check")

  val error = baseEndpoint
    .tag("health")
    .name("error health")
    .description("Health check - should fail")
    .get
    .in("health" / "error")
    .out(plainBody[String])
}
