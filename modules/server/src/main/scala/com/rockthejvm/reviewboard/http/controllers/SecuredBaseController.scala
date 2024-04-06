package com.rockthejvm.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.Endpoint
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.domain.data.UserID

/** A base controller for all secured endpoints
  *
  * @param jwtService
  */
trait SecuredBaseController(jwtService: JWTService) extends BaseController:
  /** Enriches an endpoint with security logic
    */
  extension [I, O, R](endpoint: Endpoint[String, I, Throwable, O, R])
    /** ZIO security logic for a server endpoint
      *
      * Extracts the user ID from the request and verifies the JWT token
      * @param logic,
      *   curryied function from user ID to request to response
      * @return
      */
    def securedServerLogic(logic: UserID => I => Task[O]): ServerEndpoint[R, Task] =
      endpoint
        .zServerSecurityLogic(token => jwtService.verifyToken(token))
        .serverLogic(userId => input => logic(userId)(input))
