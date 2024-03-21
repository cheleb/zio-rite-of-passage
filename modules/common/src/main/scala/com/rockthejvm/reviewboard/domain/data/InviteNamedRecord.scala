package com.rockthejvm.reviewboard.domain.data

import zio.json.JsonCodec

final case class InviteNamedRecord(companyId: Long, companyName: String, nInvites: Int) derives JsonCodec
