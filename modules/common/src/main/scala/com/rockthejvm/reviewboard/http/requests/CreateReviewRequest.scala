package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

final case class CreateReviewRequest(
    companyId: Long,
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
) derives JsonCodec
