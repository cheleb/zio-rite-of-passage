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
import com.rockthejvm.reviewboard.core.Session
import org.scalajs.dom.HTMLButtonElement
import org.scalajs.dom.HTMLDivElement
import com.raquo.laminar.nodes.ReactiveHtmlElement

trait FormState {

  def showStatus: Boolean

  def maybeSuccess: Option[String]

  def validationErrors: List[String]

  def mayBeError: Option[String] = validationErrors.headOption

  def hasErrors: Boolean = validationErrors.nonEmpty

  def maybeStatus = mayBeError.map(Left(_)).orElse(maybeSuccess.map(Right(_))).filter(_ => showStatus)

}

abstract class FormPage[S <: FormState](title: String) {

  def basicState: S

  val stateVar: Var[S] = Var(basicState)

  def renderChildren(): List[ReactiveHtmlElement[dom.html.Element]]

  final def apply() = div(
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
        div(cls := "top-section", h1(span(title))),
        children <-- stateVar.signal
          .map(_.maybeStatus)
          .map(renderStatus)
          .map(_.toList)
      ),
      form(
        nameAttr := "signin",
        cls      := "form",
        idAttr   := "form",
        // an input of type text
        renderChildren()
      )
    )
  )

  def renderStatus(status: Option[Either[String, String]]): List[HtmlElement] = status.map {
    case Left(msg) =>
      div(
        cls := "page-status-errors",
        msg
      )

    case Right(msg) =>
      div(
        cls := "page-status-success",
        msg
      )

  }.toList

  def renderInput(
      name: String,
      uid: String,
      kind: String,
      isRequired: Boolean,
      plcHolder: String,
      updateFn: (S, String) => S
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
