package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint

trait BaseController {

  val routes: List[ServerEndpoint[Any, Task]]

}
