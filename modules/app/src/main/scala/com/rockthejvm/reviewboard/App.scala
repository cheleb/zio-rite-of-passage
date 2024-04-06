package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L._
import com.rockthejvm.reviewboard.components._
import com.rockthejvm.reviewboard.core.Session
import frontroute.LinkHandler
import org.scalajs.dom

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
