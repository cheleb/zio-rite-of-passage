package com.rockthejvm.reviewboard.pages

import zio.*
import com.raquo.laminar.api.L.{*, given}
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.components.InviteAction

object ProfilePage {
  def apply() =
    div(
      cls := "row",
      div(
        cls := "col-md-5 p-0",
        div(
          cls := "logo",
          img(
            src := Constants.logoImage,
            alt := "Rock the JVM logo"
          )
        )
      ),
      div(
        cls := "col-md-7",
        // right
        div(
          cls := "form-section",
          child.maybe <-- Session(renderContent())(renderNotLoggedIn())
        )
      )
    )
  def renderContent() = div(
    cls := "top-section",
    h1(span("Profile")),
    // Change password
    div(
      cls := "profile-section",
      h3(span("Account settings")),
      Anchors.renderNavLink("Change password", "/change-password")
    ),
    // Invites
    InviteAction()
  )

  def renderNotLoggedIn() = div(
    cls := "top-section",
    h1("Please log in to access your profile")
  )

}
