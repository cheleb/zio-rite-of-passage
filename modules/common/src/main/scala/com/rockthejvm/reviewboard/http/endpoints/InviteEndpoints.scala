package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.*
import sttp.tapir.json.zio.*
import sttp.tapir.generic.auto.*
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.http.responses.*

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
}
