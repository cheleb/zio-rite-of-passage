package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import frontroute.*
import com.rockthejvm.reviewboard.pages.*

object Router:
  def apply() =
    mainTag(
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
          path("profile") { // Localhost:8080/signup
            ProfilePage()
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
          path("post") { // Localhost:8080/post
            CreateCompanyPage()
          },
          noneMatched { // Localhost:8080/whatever
            NotFoundPage()
          }
        )
      )
    )
