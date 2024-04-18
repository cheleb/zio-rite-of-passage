package com.rockthejvm.reviewboard.services

import zio.*
import zio.test.*

import java.time.Instant

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.domain.data.ReviewSummary
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import com.rockthejvm.reviewboard.config.SummaryConfig

object ReviewServiceSpec extends ZIOSpecDefault {

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

  val badReview = Review(
    id = 2,
    companyId = 1,
    userId = 1,
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 1,
    review = "All bad",
    created = Instant.now(),
    updated = Instant.now()
  )

  val reviewRepositoryLayer = ZLayer.succeed(new ReviewRepository {

    override def insertSummary(companyId: Long, summary: String): Task[ReviewSummary] =
      ZIO.succeed(ReviewSummary(companyId, summary, Instant.now()))

    override def getSummary(companyId: Long): Task[Option[ReviewSummary]] = ZIO.none

    override def deleteByCompanyId(companyId: Long): Task[List[Review]] = ZIO.succeed(List(goodReview, badReview))

    override def create(review: Review): Task[Review] =
      ZIO.succeed(goodReview)

    override def getById(reviewId: Long): Task[Option[Review]] =
      ZIO.succeed {
        reviewId match {
          case 1 => Some(goodReview)
          case 2 => Some(badReview)
          case _ => None
        }
      }

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
      ZIO.succeed {
        companyId match {
          case 1 => List(goodReview, badReview)
          case _ => List()
        }
      }

    override def getByUserId(userId: Long): Task[List[Review]] =
      ZIO.succeed {
        userId match {
          case 1 => List(goodReview, badReview)
          case _ => List()
        }
      }

    override def delete(reviewId: Long): Task[Review] =
      getById(reviewId).someOrFail(new Exception("Review not found"))

    override def update(reviewId: Long, op: Review => Review): Task[Review] =
      getById(reviewId).someOrFail(new Exception("Review not found")).map(op)

  })

  val stubOpenAIService = ZLayer.succeed(new OpenAIService {

    override def getCompletion(prompt: String): Task[Option[String]] = ZIO.none

  })

  val summaryConfigLayer = ZLayer.succeed(SummaryConfig(1, 2))

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewServiceSpec")(
      test("create a review") {
        for {
          service <- ZIO.service[ReviewService]
          review <- service.create(
            CreateReviewRequest(
              companyId = goodReview.companyId,
              management = goodReview.management,
              culture = goodReview.culture,
              salary = goodReview.salary,
              benefits = goodReview.benefits,
              wouldRecommend = goodReview.wouldRecommend,
              review = goodReview.review
            ),
            1
          )
        } yield assertTrue(
          review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
        )

      },
      test("get a review by id") {
        for {
          service  <- ZIO.service[ReviewService]
          review   <- service.getById(1)
          notFound <- service.getById(999)
        } yield assertTrue(
          review.contains(goodReview) &&
            notFound.isEmpty
        )
      },
      test("get reviews by company id") {
        for {
          service <- ZIO.service[ReviewService]
          reviews <- service.getByCompanyId(1)
          empty   <- service.getByCompanyId(999)
        } yield assertTrue(
          reviews.contains(goodReview) &&
            reviews.contains(badReview) &&
            empty.isEmpty
        )
      },
      test("get reviews by user id") {
        for {
          service <- ZIO.service[ReviewService]
          reviews <- service.getByUserId(1)
          empty   <- service.getByUserId(999)
        } yield assertTrue(
          reviews.contains(goodReview) &&
            reviews.contains(badReview) &&
            empty.isEmpty
        )
      }
    ).provide(
      summaryConfigLayer,
      ReviewServiceLive.layer,
      reviewRepositoryLayer,
      stubOpenAIService
    )
}
