package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.rockthejvm.reviewboard.common.Constants
import zio.*
import zio.prelude.*
import zio.prelude.ZValidation.Failure
import zio.prelude.ZValidation.Success
import com.rockthejvm.reviewboard.core.ZJS.*
import com.rockthejvm.reviewboard.http.requests.LoginRequest
import sttp.client3.*

object LoginPage {

  case class State(
      email: String = "",
      password: String = "",
      showStatus: Boolean = false,
      upstreamErrors: Option[String] = None
  ) {

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

    def hasErrors: Boolean = validationErrors.nonEmpty

  }

  val stateVar = Var(State())

  val submitter = Observer[State] { state =>
    if state.hasErrors then
      println("Errors")
      stateVar.update(_.copy(showStatus = true))
    else
      useBackend(_.user.loginEndpoint(LoginRequest(state.email, state.password)))
        .map { userToken =>
          stateVar.set(State())
          BrowserNavigation.replaceState("/")
        }
        .tapError(e =>
          println(e.getMessage)
          ZIO.succeed(stateVar.update(_.copy(showStatus = true, upstreamErrors = Option(e.getMessage))))
        )
        .runJs
  }

  def renderError(msg: String) =
    div(
      cls := "page-status-errors",
      msg
    )

  def apply() = div(
    cls := "row",
    div(
      cls := "col-md-5 p-0",
      div(
        cls := "logo",
        img(
          src := Constants.logoImage,
          alt := "Rock the JVM logo"
        )
      )
    ),
    div(
      cls := "col-md-7",
      // right
      div(
        cls := "form-section",
        div(cls := "top-section", h1(span("Log In"))),
        children <-- stateVar.signal.map { state =>
          if state.showStatus then
            state.validationErrors.map(renderError)
          else Nil
        },
        div(
          cls := "page-status-success",
          "This is a success"
        ),
        form(
          nameAttr := "signin",
          cls      := "form",
          idAttr   := "form",
          // an input of type text
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
          button(
            `type` := "button",
            "Log Isn",
            onClick.preventDefault.mapTo(stateVar.now()) --> submitter
          )
        )
      )
    )
  )
  def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      plcHolder: String,
      updateFn: (State, String) => State
  ) =
    div(
      cls := "row",
      div(
        cls := "col-md-12",
        div(
          cls := "form-input",
          label(
            forId := uid,
            cls   := "form-label",
            if isRequired then span("*") else span(),
            name
          ),
          input(
            `type`      := kind,
            cls         := "form-control",
            idAttr      := uid,
            placeholder := plcHolder,
            inContext(_.events(onInput.mapToValue).throttle(2000) --> stateVar.updater(updateFn))
          )
        )
      )
    )
}
