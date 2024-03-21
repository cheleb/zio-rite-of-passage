package com.rockthejvm.reviewboard.services

import zio.*
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.repositories.InviteRepository

trait InviteService {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
  def sendInvite(userName: String, companyId: Long, receivers: List[String]): Task[Int]
  def addPack(userName: String, companyId: Long): Task[Long]
}

class InviteServiceLive private (inviteRepository: InviteRepository) extends InviteService {

  override def getByUserName(userName: String): Task[List[InviteNamedRecord]] =
    inviteRepository.getByUserName(userName)

  override def sendInvite(userName: String, companyId: Long, receivers: List[String]): Task[Int] = ???

  override def addPack(userName: String, companyId: Long): Task[Long] = ???

}

object InviteServiceLive {
  val layer: RLayer[InviteRepository, InviteServiceLive] = ZLayer.fromFunction(InviteServiceLive(_))
}
