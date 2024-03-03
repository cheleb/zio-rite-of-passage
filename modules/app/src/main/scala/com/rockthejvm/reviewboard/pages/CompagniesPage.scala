package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.common.*
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.core.ZJS.*
import sttp.client3.impl.zio.FetchZioBackend
import sttp.client3.*
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.WebSockets
import sttp.tapir.Endpoint
import sttp.model.Uri
import ZIO.*
import com.rockthejvm.reviewboard.components.FilterPanel

object CompagniesPage {

  val filterPanel = new FilterPanel

  val companyEvents: EventStream[List[Company]] =
    useBackend(_.company.getAllEndpoint(()))
      .toEventStream.mergeWith(
        filterPanel.triggerFilters.flatMap(filter =>
          filterPanel.updateAppliedFilters(filter)
          useBackend(_.company.searchEndpoint(filter)).toEventStream
        )
      )

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
          filterPanel()
        ),
        div(
          cls := "col-lg-8",
          children <-- companyEvents.map(_.map(renderCompany))
        )
      )
    )
  )

//  private val

  private def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceHolder),
      alt := company.name
    )

  private def renderDetails(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  def fullLocationString(company: Company) =
    (company.location, company.country) match
      case (Some(loc), Some(c)) => s"$loc, $c"
      case (Some(loc), None)    => loc
      case (None, Some(c))      => c
      case (None, None)         => "Unknown location"

  def renderCompanyOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetails("location-dot", fullLocationString(company)),
      renderDetails("tags", company.tags.mkString(", "))
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
        renderCompanyPicture(company)
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
        renderCompanyOverview(company)
      ),
      renderAction(company)
    )

}
