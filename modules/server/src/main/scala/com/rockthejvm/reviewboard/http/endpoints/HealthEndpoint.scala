package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import zio.*

trait HealthEndpoint extends BaseEndpoint {
  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("health")
    .get
    .in("health")
    .out(plainBody[String])
    .description("Health check")

  val errorEndpoint = baseEndpoint
    .tag("health")
    .name("error health")
    .description("Health check - should fail")
    .get
    .in("health" / "error")
    .out(plainBody[String])
}
