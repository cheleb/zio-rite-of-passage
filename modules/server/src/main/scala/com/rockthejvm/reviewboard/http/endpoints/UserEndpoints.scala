package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import sttp.model.StatusCode

import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.http.requests.UserResponse
import com.rockthejvm.reviewboard.domain.data.UserToken

trait UserEndpoints extends BaseEndpoint {

  val createUserEndpoint =
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
  val updatePasswordEndpoint =
    baseSecuredEndpoint
      .tag("Users")
      .name("update password")
      .description("Update the password of a user")
      .in("users" / "password")
      .put
      .in(jsonBody[UpdatePasswordRequest])
      .out(jsonBody[UserResponse])

  // TODO - should be an authorized endpoint
  val deleteEndpoint =
    baseSecuredEndpoint
      .tag("Users")
      .name("delete account")
      .description("Delete the account of a user")
      .in("users")
      .delete
      .in(jsonBody[DeleteUserRequest])
      .out(jsonBody[UserResponse])

  val loginEndpoint =
    baseEndpoint
      .tag("Users")
      .name("login")
      .description("Login a user and generate a JWT token")
      .in("users" / "login")
      .post
      .in(jsonBody[LoginRequest])
      .out(jsonBody[UserToken])
}
