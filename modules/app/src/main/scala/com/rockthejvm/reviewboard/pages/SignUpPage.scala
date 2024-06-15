package com.rockthejvm.reviewboard.pages

import zio.*
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success
import zio.prelude.*

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.UserRegistrationRequest
import org.scalajs.dom
import org.scalajs.dom.html
import com.rockthejvm.reviewboard.http.endpoints.UserEndpoints

case class SignupFormState(
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
    showStatus: Boolean = false,
    upstreamStatus: Option[Either[String, String]] = None
) extends FormState {

  private def emailValidation = Validation.fromEither(
    if email.matches(Constants.emailRegex) then Right(email)
    else Left("Email must contain valid.")
  )

  private def passwordValidation = Validation.fromEither(
    if password.nonEmpty then Right(password)
    else Left("Password must be provided")
  )

  private def confirmPasswordValidation = Validation.fromEither(
    if confirmPassword == password then Right(confirmPassword)
    else Left("Passwords do not match")
  )

  private lazy val validate =
    Validation.validate(
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

object SignUpPage extends FormPage[SignupFormState]("Sign Up") {

  def basicState = SignupFormState()

  val submitter = Observer[SignupFormState] { state =>
    if state.hasErrors then
      println("Errors")
      stateVar.update(_.copy(showStatus = true))
    else
      UserEndpoints.create(UserRegistrationRequest(state.email, state.password))
        .map { user =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Right("Account created successfully."))))
        }
        .tapError(e =>
          println(e.getMessage)
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Left(e.getMessage)))))
        )
        .runJs
  }

  def renderChildren(): List[ReactiveHtmlElement[html.Element]] = List(
    renderInput(
      "Email",
      "email-input",
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
      (s, v) => s.copy(password = v, showStatus = false, upstreamStatus = None)
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
    button(
      `type` := "button",
      "Sign Up",
      onClick.preventDefault.mapTo(stateVar.now()) --> submitter
    )
  )
}
