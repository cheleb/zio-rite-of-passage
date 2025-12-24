package com.rockthejvm.reviewboard.components

import zio.*

import com.raquo.airstream.eventbus.EventBus
import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.core.ZJS.*

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints

class AddReviewCard(companyId: Long, onDisable: () => Unit, triggerBus: EventBus[Unit]) {

  case class State(
      review: Review = Review.empty(companyId),
      showErrors: Boolean = false,
      upstreamErrors: Option[String] = None
  ) {
    def hasErrors: Boolean = upstreamErrors.isDefined
  }

  val submitter = Observer[State] { state =>
    if state.hasErrors then
      stateVar.update(_.copy(showErrors = true))
    else
      ReviewEndpoints.create(CreateReviewRequest.fromReview(state.review))
        .map { _ =>
          // stateVar.update(_.copy(showErrors = true, upstreamErrors = None))
          onDisable()
        }
        .tapError(e =>
          ZIO.succeed(stateVar.update(_.copy(showErrors = true, upstreamErrors = Option(e.getMessage))))
        )
        .emitTo(triggerBus)
  }

  val stateVar = Var(State())

  def reviewCard() =
    div(
      cls := "container",
      div(
        cls := "markdown-body overview-section",
        div(
          cls := "company-description add-review",
          div(
            // score dropdowns
            div(
              cls := "add-review-scores",
              renderDropdown("Would recommend", (r, v) => r.copy(wouldRecommend = v)),
              renderDropdown("Management", (r, v) => r.copy(management = v)),
              renderDropdown("Culture", (r, v) => r.copy(culture = v)),
              renderDropdown("Salary", (r, v) => r.copy(salary = v)),
              renderDropdown("Benefits", (r, v) => r.copy(benefits = v))

              // TODO do the same for all score fields
            ),
            // text area for the text review
            div(
              cls := "add-review-text",
              label(forId := "add-review-text", "Your review - supports Markdown"),
              textArea(
                idAttr      := "add-review-text",
                cls         := "add-review-text-input",
                placeholder := "Write your review here",
                onInput.mapToValue --> stateVar.updater { (state: State, value: String) =>
                  state.copy(review = state.review.copy(review = value))
                }
              )
            ),
            button(
              `type` := "button",
              cls    := "btn btn-warning rock-action-btn",
              "Post review",
              // TODO post the review on this button
              onClick.preventDefault.mapTo(stateVar.now()) --> submitter
            ),
            a(
              href := "#",
              cls  := "add-review-cancel",
              "Cancel",
              onClick --> (_ => onDisable())
            ),
            // TODO show potential errors here
            child <-- stateVar.signal.map(s => s.upstreamErrors.filter(_ => s.showErrors)).map(maybeRenderError)
          )
        )
      )
    )
  private def renderDropdown(name: String, updateFn: (Review, Int) => Review) = {
    val selectorId = name.split(" ").map(_.toLowerCase).mkString("-")
    div(
      cls := "add-review-score",
      label(forId := selectorId, s"$name:"),
      select(
        idAttr := selectorId,
        (1 to 5).reverse.map { v =>
          option(v.toString)

          // TODO set state here
        },
        onInput.mapToValue --> stateVar.updater { (state: State, value: String) =>
          state.copy(review = updateFn(state.review, value.toInt))
        }
      )
    )
  }
  private def maybeRenderError(maybeError: Option[String]) = maybeError match {
    case None =>
      div()
    case Some(error) =>
      div(
        cls := "page-status-error",
        error
      )
  }
}
