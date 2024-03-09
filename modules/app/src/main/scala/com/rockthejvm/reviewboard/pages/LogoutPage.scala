package com.rockthejvm.reviewboard.pages

import org.scalajs.dom.html.Element

import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.laminar.api.L.*

import com.raquo.airstream.state.Var
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.core.ZJS.*

case class LogoutPageState() extends FormState {

  override def maybeSuccess: Option[String] = None

  override def validationErrors: List[String] = Nil

  override def showStatus: Boolean = false

}

object LogoutPage extends FormPage[LogoutPageState]("logout") {

  def basicState = LogoutPageState()

  override def renderChildren(): List[ReactiveHtmlElement[Element]] = List(
    div(
      onMountCallback(_ => Session.clearUserState()),
      cls := "align-center",
      h1("You are now logged out"),
      p("You can log in again if you want.")
    )
  )

}
