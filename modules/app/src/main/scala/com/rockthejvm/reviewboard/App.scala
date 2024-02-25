package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.rockthejvm.reviewboard.components.*
import frontroute.LinkHandler

object App extends App {

  val app = div(
    Header(),
    Router()
  )
    .amend(LinkHandler.bind) // For interbal links
  val container = dom.document.querySelector("#app")
  render(container, app)

}
