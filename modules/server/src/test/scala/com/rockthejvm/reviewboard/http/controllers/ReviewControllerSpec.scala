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

import com.rockthejvm.reviewboard.syntax.*

import com.rockthejvm.reviewboard.services.*
import java.time.Instant
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest

object ReviewControllerSpec extends ZIOSpecDefault {

  private given MonadError[Task] = new RIOMonadError

  val goodReview = Review(
    id = 1,
    companyId = 1,
    userId = 1,
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 10,
    review = "All good",
    created = Instant.now(),
    updated = Instant.now()
  )

  private val jwtServiceStub = new JWTService {
    override def createToken(user: User): Task[UserToken] =
      ZIO.succeed(UserToken(1, "daniel@rockthejvm.com", "token", 10))

    override def verifyToken(token: String): Task[UserID] =
      ZIO.succeed(UserID(1, "daniel@rockthejvm.com"))
  }
  private val serviceStub = new ReviewService {
    override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(
        goodReview
      )

    override def getById(id: Long): Task[Option[Review]] =
      ZIO.succeed {
        if id == 1 then Some(goodReview)
        else None
      }

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
      ZIO.succeed(
        if companyId == 1 then List(goodReview)
        else List.empty
      )

    override def getByUserId(userId: Long): Task[List[Review]] =
      ZIO.succeed(
        if userId == 1 then List(goodReview)
        else List.empty
      )

    override def getSummary(companyId: Long): Task[Option[ReviewSummary]] = ZIO.succeed(None)

    override def makeSummary(companyId: Long, userId: Long): Task[Option[ReviewSummary]] = ZIO.succeed(None)
  }

  private def backendStubZIO(endpointFun: ReviewController => ServerEndpoint[Any, Task]) =
    for
      controller <- ReviewController.makeZIO
      backend = TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
        .whenServerEndpointRunLogic(endpointFun(controller))
        .backend()
    yield backend

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewControllerSpec")(
      test("create review") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
            .post(uri"/reviews")
            .auth
            .bearer("It is me")
            .body(
              CreateReviewRequest(
                companyId = 1,
                management = 5,
                culture = 5,
                salary = 5,
                benefits = 5,
                wouldRecommend = 10,
                review = "All good"
              ).toJson
            )
            .send(backendStub)

        } yield response.body

        program.assert(
          _.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
        )
      },
      test("get review by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/reviews/1")
            .send(backendStub)
          respoonseNotFound <- basicRequest
            .get(uri"/reviews/999")
            .send(backendStub)

        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
            && respoonseNotFound.body.toOption
              .flatMap(_.fromJson[Review].toOption)
              .isEmpty
        )
      },
      test("get reviews by company id") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
            .get(uri"/reviews/company/1")
            .send(backendStub)
          respoonseNotFound <- basicRequest
            .get(uri"/reviews/company/999")
            .send(backendStub)

        } yield assertTrue(
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview))
            && respoonseNotFound.body.toOption
              .flatMap(_.fromJson[List[Review]].toOption)
              .contains(List.empty)
        )
      }
    ).provide(
      ZLayer.succeed(serviceStub),
      ZLayer.succeed(jwtServiceStub)
    )
}
