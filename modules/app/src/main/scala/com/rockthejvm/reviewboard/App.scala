package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

object App extends App {

  val container = dom.document.querySelector("#app")
  val app       = div("Hello, worldc!")
  render(container, app)

}
