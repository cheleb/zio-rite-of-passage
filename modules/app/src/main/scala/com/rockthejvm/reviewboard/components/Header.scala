package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import com.rockthejvm.reviewboard.components.Router
import frontroute.LinkHandler
import scalajs.js
import scala.scalajs.js.annotation.JSImport
import com.rockthejvm.reviewboard.common.Constants
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
                renderNavLinks()
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
  def renderNavLinks() = List(
    renderNavLink("Companies", "/companies"),
    renderNavLink("Login", "/login"),
    renderNavLink("Sign Up", "/signup")
  )
  //
  def renderNavLink(text: String, location: String) = li(
    cls := "nav-item",
    Anchors.renderNavLink(text, location, "nav-link jvm-item")
  )
