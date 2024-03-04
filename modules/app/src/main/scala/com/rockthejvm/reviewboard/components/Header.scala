package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import com.rockthejvm.reviewboard.components.Router
import frontroute.LinkHandler
import scalajs.js
import scala.scalajs.js.annotation.JSImport
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.domain.data.UserToken
object Header:
  def apply() = div(
    cls := "container-fluid p-0",
    div(
      cls := "jvm-nav",
      div(
        cls := "container",
        navTag(
          cls := "navbar navbar-expand-lg navbar-light JVM-nav",
          div(
            cls := "container",
            // TODO logo
            renderLogo(),
            button(
              cls                                         := "navbar-toggler",
              `type`                                      := "button",
              htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
              htmlAttr("data-bs-target", StringAsIsCodec) := "#navbarNav",
              htmlAttr("aria-controls", StringAsIsCodec)  := "navbarNav",
              htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
              htmlAttr("aria-label", StringAsIsCodec)     := "Toggle navigation",
              span(cls := "navbar-toggler-icon")
            ),
            div(
              cls    := "collapse navbar-collapse",
              idAttr := "navbarNav",
              ul(
                cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
                // TODO children
                children <-- Session.userState.signal.map(renderNavLinks)
              )
            )
          )
        )
      )
    )
  )

  def renderLogo() = a(
    cls  := "navbar-brand",
    href := "/",
    img(
      cls := "home-logo",
      src := Constants.logoImage,
      alt := "Rock the JVM logo"
    )
  )
  def renderNavLinks(maybeUserState: Option[UserToken]) = {
    val constantLinks = List(
      renderNavLink("Companies", "/companies")
    )
    val unauthedLinks = List(
      renderNavLink("Login", "/login"),
      renderNavLink("Sign Up", "/signup")
    )
    val authedLinks = List(
      renderNavLink("Profile", "/profile"),
      renderNavLink("Logout", "/logout")
    )

    constantLinks ++ (if (maybeUserState.isDefined) authedLinks else unauthedLinks)

  }
  //
  def renderNavLink(text: String, location: String) = li(
    cls := "nav-item",
    Anchors.renderNavLink(text, location, "nav-link jvm-item")
  )
