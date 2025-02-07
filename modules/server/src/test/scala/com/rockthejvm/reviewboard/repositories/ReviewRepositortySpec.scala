package com.rockthejvm.reviewboard.repositories

import zio.*
import zio.test.*

import java.time.Instant

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.syntax.*

object ReviewRepositortySpec extends ZIOSpecDefault with RepositorySpec("sql/reviews.sql") {

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
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("ReviewRepositortySpec")(
      test("create a review") {
        val program = for {
          repository <- ZIO.service[ReviewRepository]
          review     <- repository.create(goodReview)
        } yield review

        program.assert(review =>
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
          repository     <- ZIO.service[ReviewRepository]
          review         <- repository.create(goodReview)
          fetchedReview1 <- repository.getById(1)
          fetchedReview2 <- repository.getById(1)
          fetchedReview3 <- repository.getById(1)
        } yield assertTrue(
          fetchedReview1.contains(review) &&
            fetchedReview2.contains(review) &&
            fetchedReview3.contains(review)
        )
      },
      test("get all reviews by company id") {
        for {
          repository      <- ZIO.service[ReviewRepository]
          review1         <- repository.create(goodReview)
          review2         <- repository.create(badReview)
          fetchedReviews1 <- repository.getByCompanyId(1)
          fetchedReviews2 <- repository.getByUserId(1)
        } yield assertTrue(
          fetchedReviews1.toSet == Set(review1, review2) &&
            fetchedReviews2.toSet == Set(review1, review2)
        )
      },
      test("Edit review") {
        for {
          repository <- ZIO.service[ReviewRepository]
          review     <- repository.create(goodReview)
          updated    <- repository.update(review.id, _.copy(review = "All bad", updated = Instant.now()))
        } yield assertTrue(updated.review == "All bad" && updated.updated != review.updated)
      },
      test("Delete review") {
        for {
          repository <- ZIO.service[ReviewRepository]
          review     <- repository.create(goodReview)
          deleted    <- repository.delete(review.id)
          fetched    <- repository.getById(review.id)
        } yield assertTrue(deleted == review && fetched.isEmpty)
      }
    ).provide(
      ReviewRespositoryLive.layer,
      dataSouurceLayer,
      Repository.quillLayer,
      Scope.default
    )
}
