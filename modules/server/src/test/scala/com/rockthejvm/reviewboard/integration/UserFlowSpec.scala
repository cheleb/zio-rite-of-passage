package com.rockthejvm.reviewboard.integration

import zio.*
import zio.json.*
import zio.test.*

import com.rockthejvm.reviewboard.config.*
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.controllers.*
import com.rockthejvm.reviewboard.http.requests.DeleteUserRequest
import com.rockthejvm.reviewboard.http.requests.ForgotPasswordRequest
import com.rockthejvm.reviewboard.http.requests.LoginRequest
import com.rockthejvm.reviewboard.http.requests.RecoverPasswordRequest
import com.rockthejvm.reviewboard.http.requests.UpdatePasswordRequest
import com.rockthejvm.reviewboard.http.requests.UserRegistrationRequest
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.repositories.RecoveryTokenRepositoryLive
import com.rockthejvm.reviewboard.repositories.Repository
import com.rockthejvm.reviewboard.repositories.RepositorySpec
import com.rockthejvm.reviewboard.repositories.UserRepository
import com.rockthejvm.reviewboard.repositories.UserRepositoryLive
import com.rockthejvm.reviewboard.services.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError

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

    def postNoResponse(
        path: String,
        request: Request
//        token: Option[String] = None
    ): Task[Unit] =
      basicRequest
        .post(uri"$path")
        .body(request.toJson)
        .send(backend)
        .unit

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

  class EmailServiceProbe extends EmailService("http://localhost:1234") {

    private val mutableEmails = collection.mutable.Map[String, String]()
    override def sendEmail(to: String, subject: String, content: String): Task[Unit] = ???

    override def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit] =
      ZIO.succeed(mutableEmails.put(to, token))

    def probeTo(to: String): Task[Option[String]] = ZIO.succeed(mutableEmails.get(to))

  }

  val stubEmailServiceLayer: ULayer[EmailServiceProbe] = ZLayer.succeed(EmailServiceProbe())

  def createUser(backend: SttpBackend[Task, Nothing]) =
    backend
      .post[UserResponse](
        "/api/users",
        UserRegistrationRequest(danielEmail, "rockthejvm")
      )
  def loginUser(backend: SttpBackend[Task, Nothing]) =
    backend
      .post[UserToken](
        "/api/users/login",
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
          backend <- backendStubZIO
          _       <- createUser(backend)
          token   <- loginUser(backend)
        yield assertTrue(
          token
            .filter(_.email == danielEmail)
            .nonEmpty
        )
      },
      test("change user password") {
        for
          backend       <- backendStubZIO
          _             <- createUser(backend)
          tokenResponse <- loginUser(backend)
            .someOrFail(new RuntimeException("Authentication failed"))
          passwordChangeResponse <- backend.put[UserResponse](
            "/api/users/password",
            UpdatePasswordRequest(danielEmail, "rockthejvm", "zozo"),
            Some(tokenResponse.token)
          )
          oldTokenResponse <- backend
            .post[UserToken]("/api/users/login", LoginRequest(danielEmail, "rockthejvm"))
          newTokenResponse <- backend
            .post[UserToken]("/api/users/login", LoginRequest(danielEmail, "zozo"))
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
          backend       <- backendStubZIO
          _             <- createUser(backend)
          tokenResponse <- loginUser(backend)
            .someOrFail(new RuntimeException("Authentication failed"))
          userRepo               <- ZIO.service[UserRepository]
          someUser               <- userRepo.getByEmail(danielEmail)
          passwordChangeResponse <- backend.delete[UserResponse](
            "/api/users",
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
      },
      test("recover password") {
        for
          backend <- backendStubZIO
          _       <- createUser(backend)
          // Send a forgot password request
          _ <- backend.postNoResponse("/api/users/forgot", ForgotPasswordRequest(danielEmail))
          // Fetch the token from the
          emailServiceProbe <- ZIO.service[EmailServiceProbe]
          token             <- emailServiceProbe
            .probeTo(danielEmail)
            .someOrFail(new RuntimeException("No token found"))
          _ <- backend.postNoResponse(
            "/api/users/recover",
            RecoverPasswordRequest(danielEmail, token, "scalarulez")
          )
          oldTokenResponse <- backend
            .post[UserToken]("/api/users/login", LoginRequest(danielEmail, "rockthejvm"))
          newTokenResponse <- backend
            .post[UserToken]("/api/users/login", LoginRequest(danielEmail, "scalarulez"))
        yield assertTrue(
          oldTokenResponse.isEmpty && newTokenResponse.nonEmpty
        )
      }
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      UserRepositoryLive.layer,
      stubEmailServiceLayer,
      Repository.quillLayer,
      RecoveryTokenRepositoryLive.layer,
      dataSouurceLayer,
      ZLayer.succeed(JWTConfig("****", "rockthejvm", 1.hour)),
      ZLayer.succeed(RecoveryTokensConfig(24 * 3600)),
      Scope.default
    )
}
