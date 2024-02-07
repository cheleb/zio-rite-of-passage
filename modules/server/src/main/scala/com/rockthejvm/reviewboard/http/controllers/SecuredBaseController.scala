package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.Endpoint
import sttp.tapir.server.PartialServerEndpoint
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.domain.data.*

/** A base controller for all secured endpoints
  *
  * @param jwtService
  */
trait SecuredBaseController(jwtService: JWTService) extends BaseController {

  extension [I, O, R](endpoint: Endpoint[String, I, Throwable, O, R])
    def withSecurity: PartialServerEndpoint[String, UserID, I, Throwable, O, R, Task] =
      endpoint.serverSecurityLogic[UserID, Task](userToken =>
        jwtService.verifyToken(userToken).either
      )

}
