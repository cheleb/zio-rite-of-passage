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
import com.rockthejvm.reviewboard.domain.data.UserToken

case class ChangePasswordState(
    password: String = "",
    newPassword: String = "",
    confirmPassword: String = "",
    showStatus: Boolean = false,
    upstreamStatus: Option[Either[String, String]] = None
) extends FormState {

  private def passwordValidation = Validation.fromEither(
    if password.nonEmpty then Right(password)
    else Left("Password must be provided")
  )

  private def newPasswordValidation = Validation.fromEither(
    if newPassword.nonEmpty then Right(confirmPassword)
    else Left("Password must be provided")
  )

  private def confirmPasswordValidation = Validation.fromEither(
    if confirmPassword == newPassword then Right(confirmPassword)
    else Left("Passwords do not match")
  )

  private lazy val validate =
    Validation.validate(
      passwordValidation,
      newPasswordValidation,
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

object ChangePasswordPage extends SecuredFormPage[ChangePasswordState]("Profile") {

  def basicState = ChangePasswordState()

  def submitter(email: String): Observer[ChangePasswordState] = Observer[ChangePasswordState] { state =>
    if state.hasErrors then
      stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.updatePasswordEndpoint(UpdatePasswordRequest(
        email,
        state.password,
        state.newPassword
      )))
        .map { user =>
          stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Right("Password successfully changed."))))
        }
        .tapError(e =>
          println(e.getMessage)
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamStatus = Option(Left(e.getMessage)))))
        )
        .runJs
  }

  def renderChildren(user: UserToken): List[ReactiveHtmlElement[html.Element]] =
    renderProfile(user.email)
  def renderProfile(email: String) =
    List(
      renderInput(
        "Password",
        "password-input",
        "password",
        true,
        "Your password",
        (s, v) => s.copy(password = v, showStatus = false, upstreamStatus = None)
      ),
      renderInput(
        "New Password",
        "new password-input",
        "password",
        true,
        "Your new password",
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
      button(
        `type` := "button",
        "Sign Up",
        onClick.preventDefault.mapTo(stateVar.now()) --> submitter(email)
      )
    )
}
