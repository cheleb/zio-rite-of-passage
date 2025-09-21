package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.pages.*
import frontroute.*
import org.scalajs.dom

object Router:
  val externalUrlBus = EventBus[String]()
  def apply()        =
    mainTag(
      onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(using ctx.owner)),
      routes(
        div(
          cls := "container-fluid",
          // potentially children
          (pathEnd | path("companies")) {
            CompagniesPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignUpPage()
          },
          path("change-password") {
            ChangePasswordPage()
          },
          path("forgot-password") {
            ForgotPasswordPage()
          },
          path("recover-password") {
            RecoverPasswordPage()
          },
          path("logout") {
            LogoutPage()
          },
          path("profile") {
            ProfilePage()
          },
          path("post") {
            CreateCompanyPage()
          },
          path("company" / long) {
            companyId =>
              CompanyPage(companyId)
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )
