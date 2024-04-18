package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*

object NotFoundPage:
  def apply() =
    div(
      cls := "simple-titled-page",
      h1("Oops!"),
      h2("The page you are looking for does not exist."),
      p("Please check the URL and try again.")
    )
