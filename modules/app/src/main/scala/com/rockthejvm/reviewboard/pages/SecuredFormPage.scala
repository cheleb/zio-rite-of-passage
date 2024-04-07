package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.rockthejvm.reviewboard.core.Session
import com.rockthejvm.reviewboard.domain.data.UserToken
import org.scalajs.dom
import org.scalajs.dom.HTMLDivElement

abstract class SecuredFormPage[S <: FormState](title: String) extends FormPage[S](title) {

  def renderChildren(user: UserToken): List[ReactiveHtmlElement[dom.html.Element]]

  final def renderChildren(): List[ReactiveHtmlElement[dom.html.Element]] =
    Session.getUserState match
      case Some(user) =>
        renderChildren(user)
      case None =>
        List(
          div(
            cls := "logout-status",
            h1("You are not logged in !"),
            p("You should try log in again if you want.")
          )
        )

}
