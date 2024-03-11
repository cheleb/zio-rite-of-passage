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
import com.rockthejvm.reviewboard.domain.data.UserToken

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
