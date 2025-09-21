package com.rockthejvm.reviewboard.components

import scala.scalajs.js.Date

import com.raquo.laminar.api.L.*

object Footer {
  def apply() = div(
    cls := "main-footer",
    div(
      "Written with Scala with ❤️ at ",
      a(
        href   := "https://rockthejvm.com",
        target := "_blank",
        "Rock the JVM"
      )
    ),
    div(s"© Rock the JVM ${new Date().getFullYear()}")
  )
}
