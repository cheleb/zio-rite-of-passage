package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.domain.data.Review

import zio.*
import com.rockthejvm.reviewboard.repositories.ReviewRepository

trait ReviewService {
  def create(createReviewRequest: CreateReviewRequest, usedId: Long): Task[Review]
  def getById(reviewId: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService:
  override def create(createReviewRequest: CreateReviewRequest, usedId: Long): Task[Review] =
    repo.create(
      Review(
        id = -1L,
        companyId = createReviewRequest.companyId,
        userId = usedId,
        management = createReviewRequest.management,
        culture = createReviewRequest.culture,
        salary = createReviewRequest.salary,
        benefits = createReviewRequest.benefits,
        wouldRecommend = createReviewRequest.wouldRecommend,
        review = createReviewRequest.review,
        created = java.time.Instant.now(),
        updated = java.time.Instant.now()
      )
    )
  override def getById(reviewId: Long): Task[Option[Review]] =
    repo.getById(reviewId)
  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repo.getByCompanyId(companyId)
  override def getByUserId(userId: Long): Task[List[Review]] =
    repo.getByUserId(userId)

object ReviewServiceLive:
  val layer =
    ZLayer.fromFunction(ReviewServiceLive(_))
