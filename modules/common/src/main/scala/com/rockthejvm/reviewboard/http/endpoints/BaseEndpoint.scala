package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import com.rockthejvm.reviewboard.domain.errors.HttpError

trait BaseEndpoint {
  val baseEndpoint: Endpoint[Unit, Unit, Throwable, Unit, Any] = endpoint
    .errorOut(statusCode and plainBody[String])
    .mapErrorOut[Throwable](HttpError.decode)(HttpError.encode)
    .prependIn("api")

  val baseSecuredEndpoint: Endpoint[String, Unit, Throwable, Unit, Any] = baseEndpoint
    .securityIn(auth.bearer[String]())

}
