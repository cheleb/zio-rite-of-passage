package com.rockthejvm.reviewboard.services

import zio.*

import com.rockthejvm.reviewboard.config.Configs
import com.rockthejvm.reviewboard.config.InvitePackConfig
import com.rockthejvm.reviewboard.domain.data.*
import com.rockthejvm.reviewboard.repositories.*

trait InviteService {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
  def sendInvite(userName: String, companyId: Long, receivers: List[String]): Task[Int]
  def addInvitePack(userName: String, companyId: Long): Task[Long]
  def activatePack(packId: Long): Task[Boolean]
}

class InviteServiceLive private (
    inviteRepository: InviteRepository,
    companyRepository: CompanyRepository,
    emailService: EmailService,
    config: InvitePackConfig
) extends InviteService {

  override def getByUserName(userName: String): Task[List[InviteNamedRecord]] =
    inviteRepository.getByUserName(userName)

  override def sendInvite(userName: String, companyId: Long, receivers: List[String]): Task[Int] =
    for {
      company <- companyRepository.getById(companyId)
        .someOrFail(new RuntimeException(s"Send invites, company with ID $companyId not found."))

      nInvitesMarked <- inviteRepository.markInvite(userName, companyId, receivers.length)

      _ <- ZIO.foreachPar(receivers.take(nInvitesMarked)) { receiver =>
        emailService.sendReviewInviteEmail(userName, receiver, company)
      }

    } yield nInvitesMarked

  // Invariant violation: Only one pack per company.
  override def addInvitePack(userName: String, companyId: Long): Task[Long] =
    for {
      _ <-
        companyRepository.getById(companyId)
          .someOrFail(new RuntimeException(s"Company with ID $companyId not found."))
      currentPack <- inviteRepository.getInvitePack(userName, companyId)

      newPack <- currentPack match {
        case Some(_) => ZIO.fail(new RuntimeException(s"Invite pack for company $companyId already exists."))
        case None    => inviteRepository.addInvitePack(userName, companyId, config.nInvites)
      }

      // _ <- inviteRepository.activatePack(newPack) // FIXME - remove this line

    } yield newPack

  override def activatePack(packId: Long): Task[Boolean] =
    inviteRepository.activatePack(packId)

}

object InviteServiceLive {
  val layer =
    ZLayer.fromFunction(InviteServiceLive(_, _, _, _))

  val configuredLayer =
    Configs.makeConfigLayer[InvitePackConfig]("rockthejvm.invites") >>> layer
}
