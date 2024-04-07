package com.rockthejvm.reviewboard.pages

import zio.*

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.components.FilterPanel
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.domain.data.*

object CompagniesPage {

  val filterPanel = new FilterPanel

  val firstBatch = EventBus[List[Company]]()

  val companyEvents: EventStream[List[Company]] =
    firstBatch.events.mergeWith(
      filterPanel.triggerFilters.flatMap(filter =>
        filterPanel.updateAppliedFilters(filter)
        useBackend(_.company.searchEndpoint(filter)).toEventStream
      )
    )

  def apply() = sectionTag(
    onMountCallback(_ => useBackend(_.company.getAllEndpoint(())).emitTo(firstBatch)),
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
          filterPanel()
        ),
        div(
          cls := "col-lg-8",
          children <-- companyEvents.map(_.map(renderCompany))
        )
      )
    )
  )

  private def renderAction(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href   := company.url,
        target := "blank",
        button(
          `type` := "button",
          cls    := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(company: Company) =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        CompagnyComponents.renderCompanyPicture(company)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            company.name,
            s"/company/${company.id}",
            "company-title-link"
          )
        ),
        CompagnyComponents.renderCompanyOverview(company)
      ),
      renderAction(company)
    )

}
