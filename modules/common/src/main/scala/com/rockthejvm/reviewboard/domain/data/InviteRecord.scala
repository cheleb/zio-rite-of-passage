package com.rockthejvm.reviewboard.domain.data

final case class InviteRecord(id: Long, userName: String, companyId: Long, nInvites: Int, active: Boolean = false)
