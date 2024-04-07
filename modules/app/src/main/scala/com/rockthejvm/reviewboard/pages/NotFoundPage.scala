package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.*

object NotFoundPage:
  def apply() =
    div(
      h1("404: Not Found"),
      p("The page you are looking for does not exist.")
    )
