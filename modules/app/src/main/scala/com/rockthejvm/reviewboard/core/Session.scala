package com.rockthejvm.reviewboard.core

import com.rockthejvm.reviewboard.domain.data.UserToken
import com.raquo.laminar.api.L.{*, given}
import scala.scalajs.js.Date

object Session {
  val userState: Var[Option[UserToken]] = Var(Option.empty[UserToken])

  private val userTokenKey = "userToken"

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
        case UserToken(_, _, expiration) if expiration * 1000 > new Date().getTime() => Storage.remove(userTokenKey)
        case _                                                                       => userState.set
      }

  def clearUserState(): Unit =
    userState.set(Option.empty[UserToken])
    Storage.remove(userTokenKey)
}
