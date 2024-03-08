package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.domain.data.UserID

/** A base controller for all secured endpoints
  *
  * @param jwtService
  */
trait SecuredBaseController(jwtService: JWTService) extends BaseController:
  extension [I, O, R](endpoint: Endpoint[String, I, Throwable, O, R])
    def securedServerLogic(logic: UserID => I => Task[O]): ServerEndpoint[R, Task] =
      endpoint
        .zServerSecurityLogic(userId => jwtService.verifyToken(userId))
        .serverLogic(userId => input => logic(userId)(input))
