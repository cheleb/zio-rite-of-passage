package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.*

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

trait CompanyRepository extends WithTransaction {
  def create(company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]
  def search(companyFilter: CompanyFilter): Task[List[Company]]
  def uniqueAttributes: Task[CompanyFilter]

}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase])
    extends BaseRepository(quill)
    with CompanyRepository {

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

  override def delete(id: Long): Task[Company] =
    run(
      query[Company].filter(_.id == lift(id)).delete.returning(r => r)
    )

  override def getById(id: Long): Task[Option[Company]] =
    run(companies.filter(_.id == lift(id))).map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run(companies.filter(_.slug == lift(slug))).map(_.headOption)

  override def getAll: Task[List[Company]] =
    run(companies)

  override def search(companyFilter: CompanyFilter): Task[List[Company]] =
    if companyFilter.isEmpty then
      getAll
    else
      run(
        companies.filter(c =>
          (if lift(companyFilter.locations.isEmpty) then true
           else
             liftQuery(companyFilter.locations).contains(c.location)
          )
          &&
//          liftQuery(companyFilter.locations).contains(c.location) &&
          (if lift(companyFilter.countries.isEmpty) then true
           else
             liftQuery(companyFilter.countries).contains(c.country)
          ) &&
//          liftQuery(companyFilter.countries).contains(c.country) &&
          (if lift(companyFilter.industries.isEmpty) then true
           else
             liftQuery(companyFilter.industries).contains(c.industry)
          ) &&
//          liftQuery(companyFilter.industries).contains(c.industry) &&
          (if lift(companyFilter.tags.isEmpty) then true
           else
             query[Company].filter(_.id == c.id)
               .concatMap(_.tags)
               .filter(tag => liftQuery(companyFilter.tags.toSet).contains(tag)).nonEmpty
           // query[Company].filter(_.id == c.id)
           //   .concatMap(_.tags)
           //   .filter(tag => liftQuery(companyFilter.tags.toSet).contains(tag)).nonEmpty
          )
        )
      )
  override def uniqueAttributes: Task[CompanyFilter] = for {
    locations  <- run(companies.map(_.location).distinct).map(_.flatten)
    countries  <- run(companies.map(_.country).distinct).map(_.flatten)
    industries <- run(companies.map(_.industry).distinct).map(_.flatten)
    tags       <- run(companies.map(_.tags)).map(_.flatten.distinct)
  } yield CompanyFilter(locations, countries, industries, tags)

}

object CompanyRepositoryLive {
  val layer: URLayer[Quill.Postgres[SnakeCase], CompanyRepository] =
    ZLayer.fromFunction(CompanyRepositoryLive(_))
}
