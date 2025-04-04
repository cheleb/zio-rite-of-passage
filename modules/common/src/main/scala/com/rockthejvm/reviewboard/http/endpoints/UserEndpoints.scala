package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*

import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.requests.*

object UserEndpoints extends BaseEndpoint {

  val create =
    baseEndpoint
      .tag("Users")
      .name("register")
      .description("Register a new user")
      .in("users")
      .post
      .in(jsonBody[UserRegistrationRequest])
//      .out(statusCode(StatusCode.Created))
      .out(jsonBody[UserResponse])

  // TODO - should be an authorized endpoint
  val updatePassword =
    baseSecuredEndpoint
      .tag("Users")
      .name("update password")
      .description("Update the password of a user")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[UserResponse])

  // TODO - should be an authorized endpoint
  val delete =
    baseSecuredEndpoint
      .tag("Users")
      .name("delete account")
      .description("Delete the account of a user")
      .in("users")
      .delete
      .in(jsonBody[DeleteUserRequest])
      .out(jsonBody[UserResponse])

  val login =
    baseEndpoint
      .tag("Users")
      .name("login")
      .description("Login a user and generate a JWT token")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginRequest])
      .out(jsonBody[UserToken])

  // Forgot password
  val forgotPassword =
    baseEndpoint
      .tag("Users")
      .name("forgot password")
      .description("Send a password reset email to the user")
      .in("users" / "forgot")
      .post
      .in(jsonBody[ForgotPasswordRequest])
  // Recover password
  val recoverPassword =
    baseEndpoint
      .tag("Users")
      .name("recover password")
      .description("Recover the password of a user")
      .in("users" / "recover")
      .post
      .in(jsonBody[RecoverPasswordRequest])

}
