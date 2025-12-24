package com.rockthejvm.reviewboard.components

import scala.scalajs.*

import scala.scalajs.js.annotation.*

@js.native
@JSImport("showdown", JSImport.Default)
object MarkdownLib extends js.Object {
  @js.native
  class Converter extends js.Object {
    def makeHtml(markdown: String): String = js.native
  }
}

object Markdown {
  def toHtml(markdown: String): String = {
    MarkdownLib.Converter().makeHtml(markdown)
  }
}
