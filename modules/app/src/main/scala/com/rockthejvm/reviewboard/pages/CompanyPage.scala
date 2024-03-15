package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import zio.*
import com.rockthejvm.reviewboard.domain.data.*
import java.time.Instant
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.pages.CompagnyComponents.renderCompanyOverview
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.components.*
import com.raquo.laminar.DomApi

object CompanyPage {

  enum Status:
    case Loading
    case NOT_FOUND
    case OK(company: Company)

  val addReviewCardActive = Var(false)
  val fetchCompanyBus     = EventBus[Option[Company]]()
  val triggerRefreshBus   = EventBus[Unit]()
  val status = fetchCompanyBus.events.scanLeft(Status.Loading) {
    case (_, None)          => Status.NOT_FOUND
    case (_, Some(company)) => Status.OK(company)
  }

  val reviewsSignal: Signal[List[Review]] = {

    fetchCompanyBus.events.flatMap {
      case None => EventStream.empty
      case Some(company) =>
        def refreshReview = useBackend(_.review.getByCompanyIdEndpoint(company.id)).toEventStream
        refreshReview.mergeWith(triggerRefreshBus.events.flatMap(_ => refreshReview))

    }.scanLeft(List.empty)((_, newReviews) => newReviews)
  }
  // the render function

  def render(company: Company) =
    List(
      div(
        cls := "row jvm-companies-details-top-card",
        div(
          cls := "col-md-12 p-0",
          div(
            cls := "jvm-companies-details-card-profile-img",
            CompagnyComponents.renderCompanyPicture(company)
          ),
          div(
            cls := "jvm-companies-details-card-profile-title",
            h1(company.name),
            div(
              cls := "jvm-companies-details-card-profile-company-details-company-and-location",
              renderCompanyOverview(company)
            )
          ),
          child <-- Session.userState.signal.map(maybeRenderUserAction)
        )
      ),
      div(
        cls := "container-fluid",
        renderCompanySummary,
        child <-- addReviewCardActive.signal.map {
          case true  => AddReviewCard(company.id, () => addReviewCardActive.set(false), triggerRefreshBus).reviewCard()
          case false => div()
        },
        children <-- reviewsSignal.map(list => list.map(renderStaticReview)),
        div(
          cls := "container",
          div(
            cls := "rok-last",
            div(
              cls := "row invite-row",
              div(
                cls := "col-md-6 col-sm-6 col-6",
                span(
                  cls := "rock-apply",
                  p("Do you represent this company?"),
                  p("Invite people to leave reviews.")
                )
              ),
              div(
                cls := "col-md-6 col-sm-6 col-6",
                a(
                  href   := company.url,
                  target := "blank",
                  button(`type` := "button", cls := "rock-action-btn", "Invite people")
                )
              )
            )
          )
        )
      )
    )

  def maybeRenderUserAction(
      maybeUser: Option[UserToken]
  ) = maybeUser match
    case None =>
      div(
        cls := "jvm-companies-details-card-apply-now-btn",
        "Sign in to add a review"
      )

    case Some(userToken) =>
      div(
        cls := "jvm-companies-details-card-apply-now-btn",
        child <-- reviewsSignal.map { reviews =>
          reviews.exists(_.userId == userToken.id)
        }.map { alreadyReviewed =>
          if alreadyReviewed then
            div(
              cls := "jvm-companies-details-card-apply-now-btn",
              "You've already reviewed this company"
            )
          else
            button(
              `type` := "button",
              cls    := "btn btn-warning",
              "Add a review",
              disabled <-- addReviewCardActive.signal,
              onClick.mapTo(true) --> addReviewCardActive
            )
        }
      )

  def renderCompanySummary =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        div(
          cls := "company-description",
          "TODO company summary"
        )
      )
    )

  def renderStaticReview(review: Review) =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        // TODO add a highlight if this is "your" review
        div(
          cls := "company-description",
          cls.toggle("review-highlighted") <-- Session.userState.signal.map(_.exists(_.id == review.userId)),
          div(
            cls := "review-summary",
            renderStaticReviewDetail("Would Recommend", review.wouldRecommend),
            renderStaticReviewDetail("Management", review.management),
            renderStaticReviewDetail("Culture", review.culture),
            renderStaticReviewDetail("Salary", review.salary),
            renderStaticReviewDetail("Benefits", review.benefits)
          ),
          // TODO parse this Markdown

          injectMarkdown(review),
          div(cls := "review-posted", s"Posted ${Time.unixToHumanReadable(review.created.toEpochMilli())}"),
          child.maybe <-- Session.userState.signal
            .map(_.filter(_.id == review.userId)).map(_.map(_ =>
              div(cls := "review-posted", "Your review")
            ))
        )
      )
    )
  def injectMarkdown(review: Review) =
    div(
      cls := "review-content",
      foreignHtmlElement(
        DomApi.unsafeParseHtmlString(s"<div>${Markdown.toHtml(review.review)}</div>")
      )
    )

  def renderStaticReviewDetail(detail: String, score: Int) =
    div(
      cls := "review-detail",
      span(cls := "review-detail-name", s"$detail: "),
      (1 to score).toList.map(_ =>
        svg.svg(
          svg.cls     := "review-rating",
          svg.viewBox := "0 0 32 32",
          svg.path(
            svg.d := "m15.1 1.58-4.13 8.88-9.86 1.27a1 1 0 0 0-.54 1.74l7.3 6.57-1.97 9.85a1 1 0 0 0 1.48 1.06l8.62-5 8.63 5a1 1 0 0 0 1.48-1.06l-1.97-9.85 7.3-6.57a1 1 0 0 0-.55-1.73l-9.86-1.28-4.12-8.88a1 1 0 0 0-1.82 0z"
          )
        )
      )
    )

  def apply(companyId: Long) = {
    div(
      cls := "container-fluid the-rock",
      onMountCallback(_ =>
        useBackend(_.company.findByIdEndpoint(companyId.toString)).emitTo(fetchCompanyBus)
      ),
      children <-- status.map {
        case Status.Loading     => List(div("Loading..."))
        case Status.NOT_FOUND   => List(div("Company not found"))
        case Status.OK(company) => render(company)
      }
    )

  }

}
