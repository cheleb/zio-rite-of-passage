package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Option[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]

}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {

  import quill.*

  inline given schema: SchemaMeta[Company]  = schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given upMeta: UpdateMeta[Company]  = updateMeta[Company](_.id)

  private inline def companies = quote(query[Company])

  override def create(company: Company): Task[Company] =
    run(companies.insertValue(lift(company)).returning(i => i))

  override def update(id: Long, op: Company => Company): Task[Company] =
    for {
      company <- getById(id).someOrFailException
      updated <- run(
        companies.filter(_.id == lift(id)).updateValue(lift(op(company))).returning(r => r)
      )

    } yield updated

  override def delete(id: Long): Task[Option[Company]] =
    run(
      companies.filter(_.id == lift(id)).delete.returning(r => Some(r))
    )

  override def getById(id: Long): Task[Option[Company]] =
    run(companies.filter(_.id == lift(id))).map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run(companies.filter(_.slug == lift(slug))).map(_.headOption)

  override def getAll: Task[List[Company]] =
    run(companies)

}

object CompanyRepositoryLive {
  val layer: URLayer[Quill.Postgres[SnakeCase], CompanyRepository] =
    ZLayer.fromFunction(CompanyRepositoryLive(_))
}
