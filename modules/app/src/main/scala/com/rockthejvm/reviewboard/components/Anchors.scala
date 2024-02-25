package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.*
import org.scalajs.dom
import com.rockthejvm.reviewboard.components.Router
import frontroute.LinkHandler
import scalajs.js
import scala.scalajs.js.annotation.JSImport

//
object Anchors:
  def renderNavLink(text: String, location: String, cssClass: String = "") = a(
    href := location,
    cls  := cssClass,
    text
  )
