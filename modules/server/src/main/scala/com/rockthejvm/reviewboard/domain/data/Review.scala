package com.rockthejvm.reviewboard.domain.data

import java.time.Instant
import zio.json.JsonCodec
import zio.json.DeriveJsonCodec

final case class Review(
    id: Long,
    companyId: Long,
    userId: Long,
    // Scores
    management: Int,
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String,
    created: Instant,
    updated: Instant
)

object Review {
  given JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}
