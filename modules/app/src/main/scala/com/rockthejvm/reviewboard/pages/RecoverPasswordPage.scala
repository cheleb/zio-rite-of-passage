package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.core.ZJS._
import com.rockthejvm.reviewboard.http.requests._
import org.scalajs.dom
import org.scalajs.dom.html
import zio._
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success
import zio.prelude._

case class RecoverPasswordState(
    email: String = "",
    token: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    showStatus: Boolean = false,
    upstreamStatus: Option[Either[String, String]] = None
) extends FormState {

  private def tokenValidation = Validation.fromEither(
    if token.nonEmpty then Right(token)
    else Left("Token must be provided")
  )

  private def emailValidation = Validation.fromEither(
    if email.matches(Constants.emailRegex) then Right(email)
    else Left("Email must contain valid.")
  )

  private def passwordValidation = Validation.fromEither(
    if newPassword.nonEmpty then Right(newPassword)
    else Left("Password must be provided")
  )

  private def confirmPasswordValidation = Validation.fromEither(
    if confirmPassword == newPassword then Right(confirmPassword)
    else Left("Passwords do not match")
  )

  private lazy val validate =
    Validation.validate(
      tokenValidation,
      emailValidation,
      passwordValidation,
      confirmPasswordValidation,
      Validation.fromEither(upstreamStatus.flatMap(_.left.toOption).toLeft(()))
    )

  def validationErrors: List[String] = validate match
    case Failure(_, errors) =>
      errors.toList
    case Success(_, _) => Nil

  override def hasErrors: Boolean = validationErrors.nonEmpty

  override def maybeSuccess: Option[String] = upstreamStatus.flatMap(_.toOption)

}

object RecoverPasswordPage extends FormPage[RecoverPasswordState]("Recover Password") {

  def basicState = RecoverPasswordState()

  val submitter: Observer[RecoverPasswordState] = Observer[RecoverPasswordState] { state =>
    if state.hasErrors then
      stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.recoverPasswordEndpoint(RecoverPasswordRequest(
        state.email,
        state.token,
        state.newPassword
      )))
        .map { _ =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Right("Success, welcome back."))))
        }
        .tapError(e =>
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Left(e.getMessage)))))
        )
        .runJs
  }

  def renderChildren(): List[ReactiveHtmlElement[html.Element]] =
    List(
      renderInput(
        "Token",
        "token-input",
        "text",
        true,
        "Your token",
        (s, v) => s.copy(token = v, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Email",
        "Email-input",
        "text",
        true,
        "Your email",
        (s, v) => s.copy(email = v, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Password",
        "password-input",
        "password",
        true,
        "Your password",
        (s, v) => s.copy(newPassword = v, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "Confirm Password",
        "confirm-password-input",
        "password",
        true,
        "Confirm password",
        (s, v) => s.copy(confirmPassword = v, showStatus = false, upstreamStatus = None)
      ),
      // an input of type password
      div(
        cls := "align-center",
        button(
          `type` := "button",
          cls    := "btn btn-primary",
          "Recover Password",
          onClick.preventDefault.mapTo(stateVar.now()) --> submitter
        ),
        Anchors.renderNavLink("Need a recovery token?", "/forgot-password", "auth-link")
      )
    )
}
