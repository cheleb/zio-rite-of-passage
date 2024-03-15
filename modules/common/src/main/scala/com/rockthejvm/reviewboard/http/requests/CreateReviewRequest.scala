package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec
import com.rockthejvm.reviewboard.domain.data.Review

final case class CreateReviewRequest(
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
) derives JsonCodec

object CreateReviewRequest {
  def fromReview(review: Review): CreateReviewRequest = CreateReviewRequest(
    review.companyId,
    review.management,
    review.culture,
    review.salary,
    review.benefits,
    review.wouldRecommend,
    review.review
  )
}
