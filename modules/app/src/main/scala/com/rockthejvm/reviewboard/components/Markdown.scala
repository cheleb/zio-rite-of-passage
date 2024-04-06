package com.rockthejvm.reviewboard.components

import scala.scalajs._
import scala.scalajs.js._
import scala.scalajs.js.annotation._

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
