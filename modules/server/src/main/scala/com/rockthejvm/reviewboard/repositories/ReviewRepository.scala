package com.rockthejvm.reviewboard.repositories

import zio.*
import com.rockthejvm.reviewboard.domain.data.*
import io.getquill.jdbczio.Quill
import io.getquill.*

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]
  def deleteByCompanyId(companyId: Long): Task[List[Review]]
  def getSummary(companyId: Long): Task[Option[ReviewSummary]]
  def insertSummary(companyId: Long, summary: String): Task[ReviewSummary]
}

class ReviewRespositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {

  import quill.*

  inline given SchemaMeta[Review] = schemaMeta[Review]("reviews")
  inline given InsertMeta[Review] = insertMeta[Review](_.id, _.created, _.updated)
  inline given UpdateMeta[Review] = updateMeta[Review](_.id, _.companyId, _.userId, _.created)

  inline given SchemaMeta[ReviewSummary] = schemaMeta[ReviewSummary]("review_summaries")
  inline given InsertMeta[ReviewSummary] = insertMeta[ReviewSummary]()
  inline given UpdateMeta[ReviewSummary] = updateMeta[ReviewSummary]()

  override def delete(id: Long): Task[Review] =
    run(query[Review].filter(_.id == lift(id)).delete.returning(r => r))

  override def deleteByCompanyId(companyId: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(companyId)).delete.returningMany(r => r))

  override def getById(id: Long): Task[Option[Review]] =
    run(query[Review].filter(_.id == lift(id))).map(_.headOption)

  override def update(id: Long, op: Review => Review): Task[Review] =
    for {
      review <- getById(id).someOrFail(new RuntimeException(s"Review $id not found"))
      updated <- run(
        query[Review].filter(_.id == lift(id)).updateValue(lift(op(review))).returning(r => r)
      )
    } yield updated

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    run(query[Review].filter(_.companyId == lift(companyId)))

  override def getByUserId(userId: Long): Task[List[Review]] =
    run(query[Review].filter(_.userId == lift(userId)))

  override def create(review: Review): Task[Review] =
    run(query[Review].insertValue(lift(review)).returning(r => r))

  override def getSummary(companyId: Long): Task[Option[ReviewSummary]] =
    run(query[ReviewSummary].filter(_.companyId == lift(companyId))).map(_.headOption)

  override def insertSummary(companyId: Long, summary: String): Task[ReviewSummary] =
    getSummary(companyId).flatMap {
      case Some(_) =>
        run(query[ReviewSummary].filter(_.companyId == lift(companyId)).updateValue(lift(ReviewSummary(
          companyId,
          summary,
          java.time.Instant.now()
        ))).returning(r => r))
      case None =>
        run(query[ReviewSummary].insertValue(lift(ReviewSummary(
          companyId,
          summary,
          java.time.Instant.now()
        ))).returning(r => r))
    }
}
object ReviewRespositoryLive {
  val layer = ZLayer.fromFunction(new ReviewRespositoryLive(_))
}
