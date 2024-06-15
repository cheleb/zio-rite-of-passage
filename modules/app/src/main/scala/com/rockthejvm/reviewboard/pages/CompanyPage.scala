package com.rockthejvm.reviewboard.pages

import zio.*

import com.raquo.laminar.DomApi
import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.components.*
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.requests.InvitePackRequest
import com.rockthejvm.reviewboard.pages.CompagnyComponents.renderCompanyOverview
import com.rockthejvm.reviewboard.http.endpoints.InviteEndpoints
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints

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

  val inviteErrorBus = EventBus[String]()

  val reviewsSignal: Signal[List[Review]] = {

    fetchCompanyBus.events.flatMapMerge {
      case None => EventStream.empty
      case Some(company) =>
        def refreshReview = ReviewEndpoints.getByCompanyId(company.id).toEventStream
        refreshReview.mergeWith(triggerRefreshBus.events.flatMapMerge(_ => refreshReview))
    }.scanLeft(List.empty)((_, newReviews) => newReviews)
  }

  def startPaymentFlow(company: Company) =
    InviteEndpoints.addPackPromoted(InvitePackRequest(company.id))
      .tapError(error => ZIO.succeed(inviteErrorBus.emit(error.getMessage())))
      .emitTo(Router.externalUrlBus)

  // the render function Strin
  def renderInviteAction(company: Company) =
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
            button(
              `type` := "button",
              cls    := "rock-action-btn",
              disabled <-- inviteErrorBus.events.mapTo(true).startWith(false),
              "Invite people"
            ),
            onClick.mapToUnit --> (_ => startPaymentFlow(company)),
            child <-- inviteErrorBus.events.map { error =>
              div(
                cls := "alert alert-danger",
                error
              )
            }
          )
        )
      )
    )

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
        renderCompanySummary(company),
        child <-- addReviewCardActive.signal.map {
          case true  => AddReviewCard(company.id, () => addReviewCardActive.set(false), triggerRefreshBus).reviewCard()
          case false => div()
        },
        children <-- reviewsSignal.map(list => list.map(renderStaticReview)),
        child.maybe <-- Session.whenActive(renderInviteAction(company))
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

  def renderCompanySummary(company: Company) = {
    val summaryBus      = EventBus[Option[ReviewSummary]]()
    val buttonStatusBus = EventBus[Option[String]]()

    val getSummary = ReviewEndpoints.getSummary(company.id)

    val refresher = Observer[Unit] { _ =>
      val program =
        for
          _          <- ZIO.succeed(buttonStatusBus.emit(Some("Loading...")))
          newSummary <- ReviewEndpoints.makeSummary(company.id)
          _          <- ZIO.succeed(buttonStatusBus.emit(None))
        yield newSummary

      program.emitTo(summaryBus)
    }

    div(
      onMountCallback(_ => getSummary.emitTo(summaryBus)),
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        h3(span("Review Summary")),
        div(
          cls := "company-description review-summary-contents",
          child.maybe <-- summaryBus.events.map(_.map(_.content))
        ),
        child.maybe <-- summaryBus.events.map(_.map(t =>
          div(
            cls := "review-posted",
            Time.unixToHumanReadable(t.created.toEpochMilli())
          )
        )),
        button(
          `type` := "button",
          cls    := "rock-action-btn generate-btn",
          disabled <-- summaryBus.events.map(_.map(t => Time.pastDay(t.created, 1)).getOrElse(false))
            .mergeWith(buttonStatusBus.events.mapTo(true))
            .startWith(false),
          onClick.mapToUnit --> refresher,
          child.text <-- buttonStatusBus.events.map(_.getOrElse("Generate new summary")).startWith(
            "Generate new summary"
          )
        )
      )
    )
  }

  def renderStaticReview(review: Review) =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        // TODO add a highlight if this is "your" review
        div(
          cls <-- Session.userState.signal.map(_.exists(_.id == review.userId)).map {
            case true  => "company-description review-highlighted"
            case false => "company-description"
          },
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

  def renderLoading() =
    List(div(
      cls := "simple-titled-page",
      h1("Loading...")
    ))

  def renderNotFound() =
    List(div(
      cls := "simple-titled-page",
      h1("Oops!"),
      h2("Company not found"),
      a(
        href := "/",
        "Go back to the homepage"
      )
    ))

  def apply(companyId: Long) = {
    div(
      cls := "container-fluid the-rock",
      onMountCallback(_ =>
        CompanyEndpoints.findById(companyId.toString).emitTo(fetchCompanyBus)
      ),
      children <-- status.map {
        case Status.Loading     => renderLoading()
        case Status.NOT_FOUND   => renderNotFound()
        case Status.OK(company) => render(company)
      }
    )

  }

}
