package com.rockthejvm.reviewboard.http.controllers

import zio.*

import com.rockthejvm.reviewboard.domain.errors.UnauthorizedException
import com.rockthejvm.reviewboard.http.endpoints.UserEndpoints
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.services.JWTService
import com.rockthejvm.reviewboard.services.UserService
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*

class UserController(userService: UserService, jwtService: JWTService)
    extends SecuredBaseController(jwtService) {

  val create: ServerEndpoint[Any, Task] = UserEndpoints.create.zServerLogic(request =>
    userService
      .registerUser(request.email, request.password)
      .map(user => UserResponse(user.email))
  )

  val login: ServerEndpoint[Any, Task] = UserEndpoints.login.zServerLogic(request =>
    userService
      .generateToken(request.email, request.password)
      .someOrFail(UnauthorizedException("Invalid credentials"))
  )

  val updatePassword: ServerEndpoint[Any, Task] = UserEndpoints.updatePassword
    .securedServerLogic(userId =>
      request =>
        userService
          .updatePassword(request.email, request.oldPassword, request.newPassword)
          .map(user => UserResponse(user.email))
    )

  val delete: ServerEndpoint[Any, Task] = UserEndpoints.delete
    .securedServerLogic(userId =>
      request =>
        userService
          .deleteUser(request.email, request.password)
          .map(user => UserResponse(user.email))
    )

  val forgotPassword: ServerEndpoint[Any, Task] =
    UserEndpoints.forgotPassword.serverLogic(req =>
      userService.sendPasswordRecoveryEmain(req.email).either
    )

  val recoverPassword: ServerEndpoint[Any, Task] = UserEndpoints.recoverPassword.zServerLogic(req =>
    userService
      .recoverPasswordFromToken(req.email, req.token, req.newPassword)
      .filterOrFail(identity)(UnauthorizedException("Invalid token"))
      .unit
  )
  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, login, updatePassword, delete, forgotPassword, recoverPassword)

}

object UserController {
  val makeZIO: ZIO[UserService & JWTService, Nothing, UserController] =
    for
      userService <- ZIO.service[UserService]
      jwtService  <- ZIO.service[JWTService]
    yield new UserController(userService, jwtService)
}
