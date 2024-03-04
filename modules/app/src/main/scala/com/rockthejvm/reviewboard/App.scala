package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.rockthejvm.reviewboard.components.*
import frontroute.LinkHandler
import com.rockthejvm.reviewboard.core.Session

object App extends App {

  val app = div(
    onMountCallback(_ => Session.loadUserState()),
    Header(),
    Router()
  )
    .amend(LinkHandler.bind) // For interbal links
  val container = dom.document.querySelector("#app")
  render(container, app)

}
