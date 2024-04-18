package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.*
import com.rockthejvm.reviewboard.components.*
import com.rockthejvm.reviewboard.core.Session
import frontroute.LinkHandler
import org.scalajs.dom

object App extends App {

  val app = div(
    onMountCallback(_ => Session.loadUserState()),
    Header(),
    Router(),
    Footer()
  )
    .amend(LinkHandler.bind) // For interbal links
  val container = dom.document.querySelector("#app")
  render(container, app)

}
