package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard
import reviewboard.http.requests.*
import reviewboard.http.responses.*
import reviewboard.http.responses.*
import reviewboard.domain.data.*

trait InviteEndpoints extends BaseEndpoint {
  val addPackEndpoint =
    baseSecuredEndpoint
      .tag("Invites")
      .name("Add an invites")
      .description("Get invite tokens")
      .in("invite" / "add")
      .post
      .in(jsonBody[InvitePackRequest])
      .out(stringBody)

  val inviteEndpoint =
    baseSecuredEndpoint
      .tag("Invites")
      .name("Invite")
      .description("Invites a user, sends an email to")
      .in("invite")
      .post
      .in(jsonBody[InviteRequest])
      .out(jsonBody[InviteResponse])

  val getByUserIdEndpoint =
    baseSecuredEndpoint
      .tag("Invites")
      .name("Get invites by user id")
      .description("Get invites by user id")
      // .in("invite" / path[Long]("userId"))
      .in("invite" / "all")
      .get
      .out(jsonBody[List[InviteNamedRecord]])
}
