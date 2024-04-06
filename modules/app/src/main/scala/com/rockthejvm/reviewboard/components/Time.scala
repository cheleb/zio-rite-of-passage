package com.rockthejvm.reviewboard.components

import scala.scalajs._
import scala.scalajs.js._
import scala.scalajs.js.annotation._

@js.native
@JSGlobal
class Moment extends js.Object {
  def format(): String  = js.native
  def fromNow(): String = js.native
}

@js.native
@JSImport("moment", JSImport.Default)
object MomentLib extends js.Object {
  def unix(millis: Long): Moment = js.native
}

object Time {
  def unixToHumanReadable(millis: Long): String = {
    MomentLib.unix(millis / 1000).fromNow()
  }
}
