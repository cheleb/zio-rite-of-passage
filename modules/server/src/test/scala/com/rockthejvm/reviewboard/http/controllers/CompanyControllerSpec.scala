package com.rockthejvm.reviewboard.http.controllers

import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.json.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import sttp.tapir.generic.auto.*
import sttp.client3.*
import sttp.tapir.server.ServerEndpoint

import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.syntax.*

import com.rockthejvm.reviewboard.services.*
import com.rockthejvm.reviewboard.domain.data.{Company, User, UserID, UserToken}
import com.rockthejvm.reviewboard.domain.data.CompanyFilter

object CompanyControllerSpec extends ZIOSpecDefault {

  private def backendStubZIO(endpointFun: CompanyController => ServerEndpoint[Any, Task]) =
    for
      controller <- CompanyController.makeZIO
      backend = TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointRunLogic(endpointFun(controller))
        .backend()
    yield backend

  private given MonadError[Task] = new RIOMonadError

  private val rockTheJVM = Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com")

  private val serviceStub = new CompanyService {

    override def delete(id: Long): Task[Company] = ???

    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(rockTheJVM)

    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed {
        if id == 1 then Some(rockTheJVM)
        else None
      }

    override def getBySlug(slug: String): Task[Option[Company]] =
      ZIO.succeed {
        if slug == "rock-the-jvm" then Some(rockTheJVM)
        else None
      }

    override def getAll: Task[List[Company]] =
      ZIO.succeed(List(rockTheJVM))

    override def allFilters: Task[CompanyFilter] = ???

    override def search(companyFilter: CompanyFilter): Task[List[Company]] = ???

  }

  private val jwtServiceStub = new JWTService {
    override def createToken(user: User): Task[UserToken] = ???
    override def verifyToken(token: String): Task[UserID] =
      ZIO.succeed(UserID(1, "daniel@rockthejvm.com"))
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("CompanyControllerSpec")(
      test("post company") {
        val program = for {

          backend <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"/companies")
            .auth
            .bearer("It is me")
            .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)
            .send(backend)

        } yield response.body
        program.assert { responseBody =>
          responseBody.toOption
            .flatMap(
              _.fromJson[Company].toOption
            )
            .contains(Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com"))
        }
      },
      test("get all companies") {
        val program = for {
          backend <- backendStubZIO(_.getAll)
          response <- basicRequest
            .get(uri"/companies")
            .send(backend)
        } yield response.body
        program.assert { responseBody =>
          responseBody.toOption
            .flatMap(
              _.fromJson[List[Company]].toOption
            )
            .contains(List(rockTheJVM))
        }
      },
      test("get company by id") {
        val program = for {
          backend <- backendStubZIO(_.findById)
          response <- basicRequest
            .get(uri"/companies/rock-the-jvm")
            .send(backend)
        } yield response.body
        program.assert { responseBody =>
          responseBody.toOption
            .flatMap(
              _.fromJson[Option[Company]].toOption
            )
            .contains(Some(rockTheJVM))
        }
      }
    ).provide(
      ZLayer.succeed(serviceStub),
      ZLayer.succeed(jwtServiceStub)
    )
}
