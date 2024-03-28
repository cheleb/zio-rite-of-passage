package com.rockthejvm.reviewboard.core

import com.rockthejvm.reviewboard.domain.data.UserToken
import com.raquo.laminar.api.L.{*, given}
import scala.scalajs.js.Date
import com.raquo.airstream.core.Signal
import com.raquo.laminar.receivers.ChildReceiver

object Session {
  val userState: Var[Option[UserToken]] = Var(Option.empty[UserToken])

  private val userTokenKey = "userToken"

  /** This method is used to produce an Option when the user is active.
    *
    * Convenient to render an element only when the user is active.
    *
    * See ChildReceiver.maybe for more information.
    *
    * @param callback
    * @return
    */
  def whenActive[A](callback: => A): Signal[Option[A]] =
    userState.signal.map(_.map(_ => callback))
  // Should be more clever about expiration.
  def isActive = userState.now().isDefined

  def setUserState(token: UserToken): Unit = {
    userState.set(Option(token))
    Storage.set(userTokenKey, token)
  }

  def getUserState: Option[UserToken] =
    loadUserState()
    userState.now()

  def loadUserState(): Unit =
    Storage.get[UserToken](userTokenKey)
      .foreach {
        case UserToken(_, _, _, expiration) if expiration * 1000 > new Date().getTime() => Storage.remove(userTokenKey)
        case _                                                                          => userState.set
      }

  def clearUserState(): Unit =
    userState.set(Option.empty[UserToken])
    Storage.remove(userTokenKey)
}
