package com.rockthejvm.reviewboard.domain.data

import zio.json.JsonCodec
import java.time.Instant

final case class ReviewSummary(companyId: Long, content: String, created: Instant) derives JsonCodec
