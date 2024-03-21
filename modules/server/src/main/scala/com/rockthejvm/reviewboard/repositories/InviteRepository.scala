package com.rockthejvm.reviewboard.repositories

import zio.*
import io.getquill.jdbczio.Quill
import io.getquill.SnakeCase
import com.rockthejvm.reviewboard.domain.data.*
import io.getquill.*

trait InviteRepository {
  def getByUserName(userName: String): Task[List[InviteNamedRecord]]
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
}

object InviteRepositoryLive:
  val layer = ZLayer.fromFunction(InviteRepositoryLive(_))
