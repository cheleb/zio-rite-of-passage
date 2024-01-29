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
import com.rockthejvm.reviewboard.repositories.UserRepositoryLive
import com.rockthejvm.reviewboard.repositories.Repository
import com.rockthejvm.reviewboard.repositories.RepositorySpec
import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.http.requests.UserResponse

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec("sql/integration.sql") {
  // Http controller
  // Service
  // Repository

  private given MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO =
    for
      controller <- UserController.makeZIO
      backend = TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointsRunLogic(controller.routes)
        .backend()
    yield backend

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("UserFlowSpec")(
      test("post user") {
        for
          backend <- backendStubZIO
          response <- basicRequest
            .post(uri"/users")
            .body(UserRegistrationRequest("daniel@rockthejvm.com", "rockthejvm").toJson)
            .send(backend)
            .map(_.body)
            .map(_.toOption.flatMap(json => json.fromJson[UserResponse].toOption))
        yield assertTrue(response.contains(UserResponse("daniel@rockthejvm.com")))
      }
    ).provide(
      UserServiceLive.layer,
      JWTServiceLive.layer,
      UserRepositoryLive.layer,
      Repository.quillLayer,
      dataSouurceLayer,
      ZLayer.succeed(JWTConfig("****", "rockthejvm", 1.hour)),
      Scope.default
    )
}
