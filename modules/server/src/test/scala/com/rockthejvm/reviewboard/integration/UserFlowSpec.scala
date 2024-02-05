package com.rockthejvm.reviewboard.integration

import zio.*
import zio.test.*

import zio.json.*

import com.rockthejvm.reviewboard.services.*

import com.rockthejvm.reviewboard.http.controllers.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import sttp.client3.*
import com.rockthejvm.reviewboard.http.requests.UserRegistrationRequest
import com.rockthejvm.reviewboard.repositories.UserRepository
import com.rockthejvm.reviewboard.repositories.UserRepositoryLive
import com.rockthejvm.reviewboard.repositories.Repository
import com.rockthejvm.reviewboard.repositories.RepositorySpec
import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.http.responses.*
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.domain.data.UserToken
import sttp.client3.SttpBackend
import sttp.model.Method
import com.rockthejvm.reviewboard.http.requests.UpdatePasswordRequest
import com.rockthejvm.reviewboard.repositories.RecoveryTokenRepositoryLive

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec("sql/integration.sql") {
  // Http controller
  // Service
  // Repository

  private val danielEmail = "daniel@rockthejvm.com"

  private given MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO =
    for
      controller <- UserController.makeZIO
      backend = TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointsRunLogic(controller.routes)
        .backend()
    yield backend

  extension [Request: JsonCodec](backend: SttpBackend[Task, Nothing])
    def sendRequest[Response: JsonCodec](
        method: Method,
        path: String,
        request: Request,
        token: Option[String] = None
    ): Task[Option[Response]] =
      basicRequest
        .method(method, uri"$path")
        .auth
        .bearer(token.getOrElse(""))
        .body(request.toJson)
        .send(backend)
        .map(_.body)
        .map(_.toOption.flatMap(json => json.fromJson[Response].toOption))

    def post[Response: JsonCodec](
        path: String,
        request: Request,
        token: Option[String] = None
    ): Task[Option[Response]] =
      sendRequest[Response](Method.POST, path, request, token)
    def put[Response: JsonCodec](
        path: String,
        request: Request,
        token: Option[String] = None
    ): Task[Option[Response]] =
      sendRequest[Response](Method.PUT, path, request, token)
    def delete[Response: JsonCodec](
        path: String,
        request: Request,
        token: Option[String] = None
    ): Task[Option[Response]] =
      sendRequest[Response](Method.DELETE, path, request, token)

  def createUser(backend: SttpBackend[Task, Nothing]) =
    backend
      .post[UserResponse](
        "/users",
        UserRegistrationRequest(danielEmail, "rockthejvm")
      )
  def loginUser(backend: SttpBackend[Task, Nothing]) =
    backend
      .post[UserToken](
        "/users/login",
        LoginRequest(danielEmail, "rockthejvm")
      )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserFlowSpec")(
      test("create user") {
        for
          backend  <- backendStubZIO
          response <- createUser(backend)
        yield assertTrue(response.contains(UserResponse(danielEmail)))
      },
      test("create and log in user") {
        for
          backend  <- backendStubZIO
          response <- createUser(backend)
          token    <- loginUser(backend)
        yield assertTrue(
          token
            .filter(_.email == danielEmail)
            .nonEmpty
        )
      },
      test("change user password") {
        for
          backend            <- backendStubZIO
          createUserResponse <- createUser(backend)
          tokenResponse <- loginUser(backend)
            .someOrFail(new RuntimeException("Authentication failed"))
          passwordChangeResponse <- backend.put[UserResponse](
            "/users/password",
            UpdatePasswordRequest(danielEmail, "rockthejvm", "zozo"),
            Some(tokenResponse.token)
          )
          oldTokenResponse <- backend
            .post[UserToken]("/users/login", LoginRequest(danielEmail, "rockthejvm"))
          newTokenResponse <- backend
            .post[UserToken]("/users/login", LoginRequest(danielEmail, "zozo"))
        yield assertTrue(
          passwordChangeResponse.contains(
            UserResponse(
              danielEmail
            )
          ) && oldTokenResponse.isEmpty && newTokenResponse.nonEmpty
        )
      },
      test("delete user") {
        for
          backend            <- backendStubZIO
          createUserResponse <- createUser(backend)
          tokenResponse <- loginUser(backend)
            .someOrFail(new RuntimeException("Authentication failed"))
          userRepo <- ZIO.service[UserRepository]
          someUser <- userRepo.getByEmail(danielEmail)
          passwordChangeResponse <- backend.delete[UserResponse](
            "/users",
            DeleteUserRequest(danielEmail, "rockthejvm"),
            Some(tokenResponse.token)
          )
          noneUser <- userRepo.getByEmail(danielEmail)
        yield assertTrue(
          passwordChangeResponse.contains(
            UserResponse(
              danielEmail
            )
          ) && noneUser.isEmpty && someUser.nonEmpty
        )
      }
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      EmailServiceLive.configuredLayer,
      UserRepositoryLive.layer,
      Repository.quillLayer,
      RecoveryTokenRepositoryLive.configuredLayer,
      dataSouurceLayer,
      ZLayer.succeed(JWTConfig("****", "rockthejvm", 1.hour)),
      Scope.default
    )
}
