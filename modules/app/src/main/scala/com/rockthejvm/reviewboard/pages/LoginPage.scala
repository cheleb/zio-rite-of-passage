package com.rockthejvm.reviewboard.pages

import zio.*
import zio.prelude.*
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.endpoints.UserEndpoints
import com.rockthejvm.reviewboard.http.requests.*
import frontroute.*

case class LoginState(
    email: String = "",
    password: String = "",
    showStatus: Boolean = false,
    upstreamErrors: Option[String] = None
) extends FormState {

  private def emailValidation = Validation.fromEither(
    if email.matches(Constants.emailRegex) then Right(email)
    else Left("Email must contain valid.")
  )

  private def passwordValidation = Validation.fromEither(
    if password.nonEmpty then Right(password)
    else Left("Password must be provided")
  )

  private lazy val validate =
    Validation.validate(emailValidation, passwordValidation, Validation.fromEither(upstreamErrors.toLeft(())))

  def validationErrors: List[String] = validate match
    case Failure(_, errors) =>
      errors.toList
    case Success(_, _) => Nil

  override def hasErrors: Boolean = validationErrors.nonEmpty

  override def maybeSuccess: Option[String] = None

}

object LoginPage extends FormPage[LoginState]("Log In") {

  def basicState = LoginState()

  val submitter = Observer[LoginState] { state =>
    if state.hasErrors then
      stateVar.update(_.copy(showStatus = true))
    else
      UserEndpoints.login(LoginRequest(state.email, state.password))
        .map { userToken =>
          Session.setUserState(userToken)
          stateVar.set(LoginState())
          BrowserNavigation.replaceState("/")
        }
        .tapError(e =>
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamErrors = Option(e.getMessage))))
        )
        .runJs
  }

  def renderChildren() =
    List(
      renderInput(
        "Email",
        "email-input",
        "text",
        true,
        "Your email",
        (s, v) => s.copy(email = v, showStatus = false, upstreamErrors = None)
      ),
      renderInput(
        "Password",
        "password-input",
        "password",
        true,
        "Your password",
        (s, v) => s.copy(password = v, showStatus = false, upstreamErrors = None)
      ),
      // an input of type password
      div(
        cls := "align-center",
        button(
          cls    := "btn btn-primary",
          `type` := "button",
          "Log in",
          onClick.preventDefault.mapTo(stateVar.now()) --> submitter
        ),
        Anchors.renderNavLink("Forgot password", "/forgot-password", "auth-link")
      )
    )
}
