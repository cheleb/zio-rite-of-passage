package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import zio.*
import zio.prelude.*
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.http.requests.*
import com.rockthejvm.reviewboard.core.ZJS.*
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom.html
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.components.Anchors

case class ForgotPasswordState(
    email: String = "",
    showStatus: Boolean = false,
    upstreamStatus: Option[Either[String, String]] = None
) extends FormState {

  private def emailValidation = Validation.fromEither(
    if email.matches(Constants.emailRegex) then Right(email)
    else Left("Email must contain valid.")
  )

  private lazy val validate =
    Validation.validate(
      emailValidation,
      Validation.fromEither(upstreamStatus.flatMap(_.left.toOption).toLeft(()))
    )

  def validationErrors: List[String] = validate match
    case Failure(_, errors) =>
      errors.toList
    case Success(_, _) => Nil

  override def hasErrors: Boolean = validationErrors.nonEmpty

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

}

object ForgotPasswordPage extends FormPage[ForgotPasswordState]("Forgot Password") {

  def basicState = ForgotPasswordState()

  val submitter: Observer[ForgotPasswordState] = Observer[ForgotPasswordState] { state =>
    if state.hasErrors then
      stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.forgotPasswordEndpoint(ForgotPasswordRequest(
        state.email
      )))
        .map { user =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Right("Password request submited."))))
        }
        .tapError(e =>
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Left(e.getMessage)))))
        )
        .runJs
  }

  def renderChildren(): List[ReactiveHtmlElement[html.Element]] =
    List(
      renderInput(
        "Email",
        "Email-input",
        "text",
        true,
        "Your email",
        (s, v) => s.copy(email = v, showStatus = false, upstreamStatus = None)
      ),
      // an input of type password
      div(
        cls := "align-center",
        button(
          `type` := "button",
          cls    := "btn btn-primary",
          "Recover Password",
          onClick.preventDefault.mapTo(stateVar.now()) --> submitter
        )
      ),
      Anchors.renderNavLink("Have a password recovery token?", "/recover-password", "auth-link")
    )
}
