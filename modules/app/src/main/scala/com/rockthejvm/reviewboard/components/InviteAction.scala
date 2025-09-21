package com.rockthejvm.reviewboard.components

import zio.*

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.domain.data.InviteNamedRecord
import com.rockthejvm.reviewboard.http.endpoints.InviteEndpoints
import com.rockthejvm.reviewboard.http.requests.InviteRequest

object InviteAction {

  val inviteListBus = EventBus[List[InviteNamedRecord]]()
  def apply() =
    div(
      cls := "profile-section",
      h3(span("Invites")),
      onMountCallback(_ => refreshInviteList().emitTo(inviteListBus)),
      children <-- inviteListBus.events.map(_.map(renderInviteSection))
    )

  def refreshInviteList() =
    InviteEndpoints.getByUserId(())

  def renderInviteSection(record: InviteNamedRecord) = {
    val emailListVar  = Var(Array.empty[String])
    val maybeErrorVar = Var(Option.empty[String])

    val inviteSubmitter = Observer[Unit] { _ =>
      val emails = emailListVar.now()
      if emails.isEmpty then
        maybeErrorVar.update(_ => Some("Please provide at least one email."))
      else {
        if emails.exists(!_.matches(Constants.emailRegex)) then
          maybeErrorVar.update(_ => Some("Please provide valid emails."))
        else {
          val refreshProgram = for {
            _       <- InviteEndpoints.invite(InviteRequest(record.companyId, emails.toList))
            invites <- refreshInviteList()
          } yield invites

          maybeErrorVar.set(None)
          refreshProgram.emitTo(inviteListBus)
        }
      }

    }

    // <-- -->
    div(
      cls := "invite-section",
      h5(span(record.companyName)),
      p(s"Invite left: ${record.nInvites}"),
      textArea(
        cls         := "invite-area",
        rows        := 3,
        placeholder := "Emails, one per line.",
        onInput.mapToValue.map(_.split("\n").map(_.trim).filter(_.nonEmpty)) --> emailListVar.writer
      ),
      button(
        cls := "btn btn-primary",
        "Invite them",
        onClick.mapTo(()) --> inviteSubmitter
      ),
      child.maybe <-- maybeErrorVar.signal.map(_.map(e =>
        div(
          cls := "page-status-errors",
          div(cls := "error", e)
        )
      )),
      children <-- emailListVar.signal.map(i => i.toSeq.map(e => div(e)))
    )
  }
}
