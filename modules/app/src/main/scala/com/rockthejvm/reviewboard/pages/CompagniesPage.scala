package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.rockthejvm.reviewboard.components.Anchors

object CompagniesPage {
  def apply() = sectionTag(
    cls := "section-1",
    div(
      cls := "container company-list-hero",
      h1(
        cls := "company-list-title",
        "Rock the JVM Companies Board"
      )
    ),
    div(
      cls := "container",
      div(
        cls := "row jvm-recent-companies-body",
        div(
          cls := "col-lg-4",
          div("TODO filter panel here")
        ),
        div(
          cls := "col-lg-8",
          sketchCompany(),
          sketchCompany()
        )
      )
    )
  )

  def sketchCompany() =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        img(
          cls := "img-fluid",
          src := "TODO company placeholder",
          alt := "Dummy company"
        )
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            "Dummy company",
            s"/company/dummy",
            "company-title-link"
          )
        ),
        div(
          cls := "company-summary",
          div(
            cls := "company-detail",
            i(cls := s"fa fa-location-dot company-detail-icon"),
            p(
              cls := "company-detail-value",
              "Some city, some country"
            )
          ),
          div(
            cls := "company-detail",
            i(cls := s"fa fa-tags company-detail-icon"),
            p(
              cls := "company-detail-value",
              "tag 1, tag 2"
            )
          )
        )
      ),
      div(
        cls := "jvm-recent-companies-card-btn-apply",
        a(
          href   := "https://todo.com",
          target := "blank",
          button(
            `type` := "button",
            cls    := "btn btn-danger rock-action-btn",
            "View"
          )
        )
      )
    )

}
