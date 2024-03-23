package com.rockthejvm.reviewboard.repositories

import zio.*
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import com.rockthejvm.reviewboard.domain.data.*
import io.getquill.*

trait InviteRepository {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
  def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]]
  def addInvitePack(userName: String, companyId: Long, nInvites: Int): Task[Long]
  def activatePack(id: Long): Task[Boolean]
  def markInvite(userName: String, companyId: Long, nInvites: Int): Task[Int]
}

class InviteRepositoryLive private (quill: Quill.Postgres[SnakeCase])
    extends BaseRepository(quill) with InviteRepository {

  import quill.*

  inline given schema: SchemaMeta[InviteRecord]  = schemaMeta[InviteRecord]("invites")
  inline given SchemaMeta[Company]               = schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[InviteRecord] = insertMeta[InviteRecord](_.id)
  inline given upMeta: UpdateMeta[InviteRecord]  = updateMeta[InviteRecord](_.id)

  override def getByUserName(userName: String): Task[List[InviteNamedRecord]] =
    run(
      for {
        record <- query[InviteRecord]
          .filter(_.userName == lift(userName))
          .filter(_.nInvites > 0)
          .filter(_.active)
        company <- query[Company] if company.id == record.companyId
      } yield InviteNamedRecord(company.id, company.name, record.nInvites)
    )

  override def getInvitePack(userName: String, companyId: Long): Task[Option[InviteRecord]] =
    run(
      query[InviteRecord]
        .filter(_.userName == lift(userName))
        .filter(_.companyId == lift(companyId))
    ).map(_.headOption)

  override def addInvitePack(userName: String, companyId: Long, nInvites: Int): Task[Long] =
    run(
      query[InviteRecord]
        .insertValue(lift(InviteRecord(0, userName, companyId, nInvites, active = false)))
        .returning(_.id)
    )

  override def activatePack(id: Long): Task[Boolean] = run(
    query[InviteRecord]
      .filter(_.id == lift(id))
      .update(_.active -> lift(true))
  ).map(_ > 0)

  override def markInvite(userName: String, companyId: Long, nInvites: Int): Task[Int] =
    for {
      pack <- getInvitePack(userName, companyId)
        .someOrFail(new RuntimeException(s"User $userName cannot send invite pack for company id $companyId."))
      nInvitesMarked = Math.min(pack.nInvites, nInvites)
      _ <- run(
        query[InviteRecord]
          .filter(_.id == lift(pack.id))
          .update(_.nInvites -> (lift(pack.nInvites) - lift(nInvitesMarked)))
      )

    } yield nInvitesMarked

}

object InviteRepositoryLive:
  val layer = ZLayer.fromFunction(InviteRepositoryLive(_))
