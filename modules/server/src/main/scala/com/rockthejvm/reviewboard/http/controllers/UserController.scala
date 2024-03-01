package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.services.UserService

import com.rockthejvm.reviewboard.http.endpoints.UserEndpoints

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

import zio.*
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.http.requests.UserRegistrationRequest
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.domain.data.UserID
import com.rockthejvm.reviewboard.domain.errors.UnauthorizedException

class UserController(userService: UserService, jwtService: JWTService)
    extends BaseController
    with UserEndpoints {

  val create: ServerEndpoint[Any, Task] = createUserEndpoint.serverLogic(request =>
    userService
      .registerUser(request.email, request.password)
      .map(user => UserResponse(user.email))
      .either
  )

  val login: ServerEndpoint[Any, Task] = loginEndpoint.serverLogic(request =>
    userService
      .generateToken(request.email, request.password)
      .someOrFail(UnauthorizedException)
      .either
  )

  val updatePassword: ServerEndpoint[Any, Task] = updatePasswordEndpoint
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic(userId =>
      request =>
        userService
          .updatePassword(request.email, request.oldPassword, request.newPassword)
          .map(user => UserResponse(user.email))
          .either
    )

  val delete: ServerEndpoint[Any, Task] = deleteEndpoint
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic(userId =>
      request =>
        userService
          .deleteUser(request.email, request.password)
          .map(user => UserResponse(user.email))
          .either
    )

  val forgotPassword: ServerEndpoint[Any, Task] =
    forgotPasswordEndpoint.serverLogic(req =>
      userService.sendPasswordRecoveryEmain(req.email).either
    )

  val recoverPassword: ServerEndpoint[Any, Task] = recoverPasswordEndpoint.serverLogic(req =>
    userService
      .recoverPasswordFromToken(req.email, req.token, req.newPassword)
      .filterOrFail(identity)(UnauthorizedException)
      .unit
      .either
  )
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, login, updatePassword, delete, forgotPassword, recoverPassword)

}

object UserController {
  def makeZIO: ZIO[UserService & JWTService, Nothing, UserController] =
    for
      userService <- ZIO.service[UserService]
      jwtService  <- ZIO.service[JWTService]
    yield new UserController(userService, jwtService)
}
