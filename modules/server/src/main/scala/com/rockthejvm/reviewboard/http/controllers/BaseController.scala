package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint
import com.rockthejvm.reviewboard.domain.data.UserID

trait BaseController {

  val routes: List[ServerEndpoint[Any, Task]]

  extension [I, O](endpoint: Endpoint[Unit, I, Throwable, O, Any])
    def zioServerLogic(logic: I => Task[O]): ServerEndpoint[Any, Task] =
      endpoint.serverLogic(i => logic(i).either)

}
