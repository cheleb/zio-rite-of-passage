package com.rockthejvm.reviewboard.core
import org.scalajs.dom

import zio.json.*

object Storage {

  def set[A: JsonCodec](key: String, value: A): Unit =
    dom.window.localStorage.setItem(key, value.toJson)

  def get[A: JsonCodec](key: String): Option[A] =
    Option(dom.window.localStorage.getItem(key))
      .filter(_.nonEmpty)
      .flatMap(_.fromJson[A].toOption)

  def remove(key: String): Unit =
    dom.window.localStorage.removeItem(key)

}
