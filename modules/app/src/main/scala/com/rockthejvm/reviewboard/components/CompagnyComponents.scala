package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.common.*
import com.rockthejvm.reviewboard.domain.data.*

object CompagnyComponents {

  def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceHolder),
      alt := company.name
    )

  def renderDetails(icon: String, value: String) =
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

}
