package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.rockthejvm.reviewboard.pages.*

object Router:
  val externalUrlBus = EventBus[String]()
  def apply() =
    mainTag(
      onMountCallback(ctx => externalUrlBus.events.foreach(url => dom.window.location.href = url)(ctx.owner)),
      routes(
        div(
          cls := "container-fluid",
          // potentially children
          (pathEnd | path("companies")) { // Localhost:8080/companies
            CompagniesPage()
          },
          path("login") { // Localhost:8080/login
            LoginPage()
          },
          path("signup") { // Localhost:8080/signup
            SignUpPage()
          },
          path("change-password") { // Localhost:8080/signup
            ChangePasswordPage()
          },
          path("forgot-password") { // Localhost:8080/forgot-password
            ForgotPasswordPage()
          },
          path("recover-password") { // Localhost:8080/recover-password
            RecoverPasswordPage()
          },
          path("logout") { // Localhost:8080/logout
            LogoutPage()
          },
          path("profile") { // Localhost:8080/profile
            ProfilePage()
          },
          path("post") { // Localhost:8080/post
            CreateCompanyPage()
          },
          path("company" / long) { // Localhost:8080/company/42
            companyId =>
              CompanyPage(companyId)
          },
          noneMatched { // Localhost:8080/whatever
            NotFoundPage()
          }
        )
      )
    )
